#!/usr/bin/env python3
"""Build the OpenYaBS GitHub wiki into a searchable MkDocs Material static site.

Reads a local checkout of https://github.com/anti43/openyabs.wiki.git, converts
GitHub Wiki links and asset references into MkDocs-compatible relative links,
generates navigation from _Sidebar.md (plus any unlisted pages), builds the
site with MkDocs Material into a staging directory, validates the output, and
atomically updates the target directory (typically docs/wiki/).
"""

from __future__ import annotations

import argparse
import gzip
import json
import os
import posixpath
import re
import shutil
import subprocess
import sys
import tempfile
from dataclasses import dataclass, field
from pathlib import Path
from urllib.parse import unquote

import yaml

WIKI_META_FILES = {"_Sidebar.md", "_Footer.md", "_Header.md"}
RAW_WIKI_PREFIXES = (
    "https://raw.githubusercontent.com/wiki/anti43/openyabs/",
    "http://raw.githubusercontent.com/wiki/anti43/openyabs/",
)
GITHUB_WIKI_PREFIXES = (
    "https://github.com/anti43/openyabs/wiki/",
    "http://github.com/anti43/openyabs/wiki/",
)
EXTERNAL_SCHEMES = ("http://", "https://", "mailto:", "tel:", "data:", "javascript:")


@dataclass
class AuditReport:
    pages_processed: int = 0
    assets_copied: int = 0
    internal_links_rewritten: int = 0
    raw_assets_localized: int = 0
    wikilinks_converted: int = 0
    unresolved_links: list[tuple[str, str]] = field(default_factory=list)
    unlisted_pages: list[str] = field(default_factory=list)


def is_google_verification_md(rel_path: str) -> bool:
    name = posixpath.basename(rel_path)
    return bool(re.fullmatch(r"google[0-9a-f]+\.html\.md", name))


def page_title_from_stem(stem: str) -> str:
    if stem == "Home":
        return "OpenYaBS Wiki"
    return stem.replace("-", " ")


def has_h1_heading(text: str) -> bool:
    lines = text.splitlines()
    in_fence = False
    fence_char = ""
    fence_len = 0
    for idx, line in enumerate(lines):
        stripped = line.lstrip()
        m_fence = re.match(r"^(`{3,}|~{3,})", stripped)
        if m_fence:
            ch = m_fence.group(1)[0]
            ln = len(m_fence.group(1))
            if not in_fence:
                in_fence = True
                fence_char = ch
                fence_len = ln
            elif ch == fence_char and ln >= fence_len and stripped[ln:].strip() == "":
                in_fence = False
            continue
        if in_fence:
            continue
        if re.match(r"^#\s+\S", line):
            return True
        if idx > 0 and re.match(r"^={3,}\s*$", line) and lines[idx - 1].strip():
            return True
    return False


def build_page_lookup(wiki_dir: Path) -> tuple[dict[str, str], dict[str, str], set[str]]:
    """Return (exact_stem_to_dst, lower_stem_to_dst, asset_relpaths)."""
    exact: dict[str, str] = {}
    lower: dict[str, str] = {}
    assets: set[str] = set()

    for root, dirs, files in os.walk(wiki_dir):
        dirs[:] = sorted(d for d in dirs if d not in {".git", ".github"})
        for filename in sorted(files):
            full = Path(root) / filename
            rel = full.relative_to(wiki_dir).as_posix()
            if filename.endswith(".md"):
                if rel in WIKI_META_FILES:
                    continue
                stem = rel[:-3]
                dst = "index.md" if rel == "Home.md" else rel
                exact[stem] = dst
                exact[stem.replace(" ", "-")] = dst
                lower[stem.lower()] = dst
                lower[stem.replace(" ", "-").lower()] = dst
                # GitHub wiki resolves pages by basename even when stored flat
                base_stem = posixpath.basename(stem)
                exact.setdefault(base_stem, dst)
                lower.setdefault(base_stem.lower(), dst)
            else:
                assets.add(rel)

    return exact, lower, assets


def resolve_page_target(
    raw_target: str,
    src_dst_rel: str,
    exact_pages: dict[str, str],
    lower_pages: dict[str, str],
    assets: set[str],
) -> tuple[str | None, str]:
    """Resolve a wiki-relative target to a MkDocs-relative target.

    Returns (resolved_target_or_None, kind) where kind is one of:
    'page', 'asset', 'unresolved'.
    """
    target = raw_target.strip()
    if not target:
        return None, "unresolved"

    # Separate optional title after whitespace only when not wrapped in <...>
    title_suffix = ""
    if target.startswith("<") and ">" in target:
        end_angle = target.index(">")
        url_part = target[1:end_angle]
        title_suffix = target[end_angle + 1 :]
    else:
        m_title = re.match(r'^(\S+)(\s+(?:"[^"]*"|\'[^\']*\'|\([^)]*\)))\s*$', target)
        if m_title:
            url_part = m_title.group(1)
            title_suffix = m_title.group(2)
        else:
            url_part = target

    if "#" in url_part:
        path_part, anchor = url_part.split("#", 1)
        anchor_suffix = "#" + anchor
    else:
        path_part = url_part
        anchor_suffix = ""

    decoded = unquote(path_part).strip()
    if decoded.startswith("./"):
        decoded = decoded[2:]
    decoded = decoded.lstrip("/")

    stem_candidate = decoded[:-3] if decoded.endswith(".md") else decoded
    dst_rel = (
        exact_pages.get(stem_candidate)
        or exact_pages.get(stem_candidate.replace(" ", "-"))
        or lower_pages.get(stem_candidate.lower())
        or lower_pages.get(stem_candidate.replace(" ", "-").lower())
    )

    src_dir = posixpath.dirname(src_dst_rel) or "."
    if dst_rel is not None:
        rel_out = posixpath.relpath(dst_rel, src_dir)
        out_url = f"{rel_out}{anchor_suffix}"
        if "(" in out_url or ")" in out_url or " " in out_url:
            out_url = f"<{out_url}>"
        return f"{out_url}{title_suffix}", "page"

    if decoded in assets:
        rel_out = posixpath.relpath(decoded, src_dir)
        out_url = f"{rel_out}{anchor_suffix}"
        if "(" in out_url or ")" in out_url or " " in out_url:
            out_url = f"<{out_url}>"
        return f"{out_url}{title_suffix}", "asset"

    return None, "unresolved"


def rewrite_link_destination(
    dest: str,
    src_rel: str,
    src_dst_rel: str,
    exact_pages: dict[str, str],
    lower_pages: dict[str, str],
    assets: set[str],
    audit: AuditReport,
) -> str:
    stripped = dest.strip()
    if not stripped or stripped.startswith("#"):
        return dest

    # Rewrite raw.githubusercontent.com/wiki/anti43/openyabs/<asset> when local
    for prefix in RAW_WIKI_PREFIXES:
        if stripped.startswith(prefix):
            rest = stripped[len(prefix) :]
            asset_part = unquote(rest.split("#", 1)[0].split("?", 1)[0]).lstrip("/")
            if asset_part in assets:
                src_dir = posixpath.dirname(src_dst_rel) or "."
                rel_out = posixpath.relpath(asset_part, src_dir)
                audit.raw_assets_localized += 1
                if "(" in rel_out or ")" in rel_out or " " in rel_out:
                    return f"<{rel_out}>"
                return rel_out
            return dest

    # Rewrite full https://github.com/anti43/openyabs/wiki/<Page> links when local
    for prefix in GITHUB_WIKI_PREFIXES:
        if stripped.startswith(prefix):
            rest = stripped[len(prefix) :]
            if rest and not rest.startswith("_"):
                resolved, kind = resolve_page_target(
                    rest, src_dst_rel, exact_pages, lower_pages, assets
                )
                if resolved is not None and kind == "page":
                    audit.internal_links_rewritten += 1
                    return resolved
            return dest

    if any(stripped.lower().startswith(scheme) for scheme in EXTERNAL_SCHEMES):
        return dest

    resolved, kind = resolve_page_target(
        stripped, src_dst_rel, exact_pages, lower_pages, assets
    )
    if resolved is not None:
        if kind == "page":
            audit.internal_links_rewritten += 1
        return resolved

    audit.unresolved_links.append((src_rel, stripped))
    return dest


def rewrite_inline_markdown(
    line: str,
    src_rel: str,
    src_dst_rel: str,
    exact_pages: dict[str, str],
    lower_pages: dict[str, str],
    assets: set[str],
    audit: AuditReport,
) -> str:
    """Rewrite Markdown links/images and [[wikilinks]] outside inline code spans."""
    out: list[str] = []
    i = 0
    n = len(line)

    while i < n:
        # Preserve inline code spans (`...`, ``...``, etc.)
        if line[i] == "`":
            j = i
            while j < n and line[j] == "`":
                j += 1
            ticks = line[i:j]
            end = line.find(ticks, j)
            if end != -1:
                out.append(line[i : end + len(ticks)])
                i = end + len(ticks)
                continue
            out.append(ticks)
            i = j
            continue

        # Convert GitHub [[wikilinks]]
        if line[i : i + 2] == "[[":
            end = line.find("]]", i + 2)
            if end != -1:
                inner = line[i + 2 : end].strip()
                if "|" in inner:
                    label, target = [p.strip() for p in inner.split("|", 1)]
                else:
                    label = inner
                    target = inner
                resolved, kind = resolve_page_target(
                    target, src_dst_rel, exact_pages, lower_pages, assets
                )
                if resolved is not None:
                    audit.wikilinks_converted += 1
                    out.append(f"[{label}]({resolved})")
                else:
                    audit.unresolved_links.append((src_rel, f"[[{inner}]]"))
                    out.append(line[i : end + 2])
                i = end + 2
                continue

        # Rewrite [text](dest) and ![alt](dest) with balanced parenthesis matching
        if line[i] == "]" and i + 1 < n and line[i + 1] == "(":
            j = i + 2
            depth = 1
            while j < n and depth > 0:
                if line[j] == "\\" and j + 1 < n:
                    j += 2
                    continue
                if line[j] == "(":
                    depth += 1
                elif line[j] == ")":
                    depth -= 1
                j += 1
            if depth == 0:
                dest = line[i + 2 : j - 1]
                new_dest = rewrite_link_destination(
                    dest, src_rel, src_dst_rel, exact_pages, lower_pages, assets, audit
                )
                out.append(f"]({new_dest})")
                i = j
                continue

        out.append(line[i])
        i += 1

    text = "".join(out)

    # Also localize raw wiki asset URLs in HTML src="..." attributes
    def replace_html_src(m: re.Match[str]) -> str:
        attr, quote, url = m.group(1), m.group(2), m.group(3)
        for prefix in RAW_WIKI_PREFIXES:
            if url.startswith(prefix):
                asset_part = unquote(url[len(prefix) :].split("#", 1)[0].split("?", 1)[0]).lstrip("/")
                if asset_part in assets:
                    src_dir = posixpath.dirname(src_dst_rel) or "."
                    rel_out = posixpath.relpath(asset_part, src_dir)
                    audit.raw_assets_localized += 1
                    return f"{attr}={quote}{rel_out}{quote}"
        return m.group(0)

    text = re.sub(r'\b(src|href)=(["\'])([^"\']+)\2', replace_html_src, text)
    return text


def transform_markdown(
    content: str,
    src_rel: str,
    src_dst_rel: str,
    exact_pages: dict[str, str],
    lower_pages: dict[str, str],
    assets: set[str],
    audit: AuditReport,
) -> str:
    lines = content.splitlines(keepends=True)
    result: list[str] = []
    in_fence = False
    fence_char = ""
    fence_len = 0

    for line in lines:
        stripped = line.lstrip()
        m_fence = re.match(r"^(`{3,}|~{3,})", stripped)
        if m_fence:
            ch = m_fence.group(1)[0]
            ln = len(m_fence.group(1))
            if not in_fence:
                in_fence = True
                fence_char = ch
                fence_len = ln
            elif ch == fence_char and ln >= fence_len and stripped[ln:].strip() == "":
                in_fence = False
            result.append(line)
            continue

        if in_fence:
            result.append(line)
        else:
            result.append(
                rewrite_inline_markdown(
                    line, src_rel, src_dst_rel, exact_pages, lower_pages, assets, audit
                )
            )

    transformed = "".join(result)

    if is_google_verification_md(src_rel):
        return "---\nsearch:\n  exclude: true\n---\n\n" + transformed

    if not has_h1_heading(transformed):
        title = page_title_from_stem(src_rel[:-3])
        transformed = f"# {title}\n\n" + transformed

    return transformed


def parse_markdown_link_balanced(text: str) -> tuple[str, str] | None:
    """Parse the first [label](target) from text using balanced parentheses."""
    start = text.find("[")
    if start == -1:
        return None
    mid = text.find("](", start + 1)
    if mid == -1:
        return None
    label = text[start + 1 : mid].strip()
    j = mid + 2
    depth = 1
    while j < len(text) and depth > 0:
        if text[j] == "(":
            depth += 1
        elif text[j] == ")":
            depth -= 1
        j += 1
    if depth != 0:
        return None
    target = text[mid + 2 : j - 1].strip()
    return label, target


def build_navigation(
    wiki_dir: Path,
    exact_pages: dict[str, str],
    lower_pages: dict[str, str],
    all_doc_files: list[str],
    audit: AuditReport,
) -> list[object]:
    """Build MkDocs nav from _Sidebar.md, appending any unlisted pages."""
    nav: list[object] = []
    seen_docs: set[str] = set()
    sidebar_path = wiki_dir / "_Sidebar.md"

    if sidebar_path.is_file():
        current_section: str | None = None
        current_items: list[dict[str, str]] = []

        def flush_section() -> None:
            nonlocal current_section, current_items
            if current_section and current_items:
                nav.append({current_section: current_items})
            current_section = None
            current_items = []

        for raw_line in sidebar_path.read_text(encoding="utf-8").splitlines():
            line = raw_line.strip()
            if not line:
                continue

            # Bold heading line: **Section** (without a link)
            m_section = re.fullmatch(r"\*\*([^*]+)\*\*", line)
            if m_section and "](" not in line:
                flush_section()
                current_section = m_section.group(1).strip()
                continue

            # Top-level link line like **[Home](Home)**
            if not line.startswith(("-", "* ")) and "](" in line:
                parsed = parse_markdown_link_balanced(line)
                if parsed:
                    label, target = parsed
                    if any(target.lower().startswith(s) for s in EXTERNAL_SCHEMES):
                        continue
                    stem = unquote(target.split("#", 1)[0])
                    stem = stem[:-3] if stem.endswith(".md") else stem
                    dst = (
                        exact_pages.get(stem)
                        or exact_pages.get(stem.replace(" ", "-"))
                        or lower_pages.get(stem.lower())
                        or lower_pages.get(stem.replace(" ", "-").lower())
                    )
                    if dst and dst not in seen_docs:
                        flush_section()
                        nav.append({label: dst})
                        seen_docs.add(dst)
                continue

            # Bullet link item under a section
            if line.startswith(("-", "*")) and "](" in line:
                parsed = parse_markdown_link_balanced(line)
                if parsed:
                    label, target = parsed
                    if any(target.lower().startswith(s) for s in EXTERNAL_SCHEMES):
                        continue
                    stem = unquote(target.split("#", 1)[0])
                    stem = stem[:-3] if stem.endswith(".md") else stem
                    dst = (
                        exact_pages.get(stem)
                        or exact_pages.get(stem.replace(" ", "-"))
                        or lower_pages.get(stem.lower())
                        or lower_pages.get(stem.replace(" ", "-").lower())
                    )
                    if dst:
                        seen_docs.add(dst)
                        if current_section:
                            current_items.append({label: dst})
                        else:
                            nav.append({label: dst})

        flush_section()

    if "index.md" in all_doc_files and "index.md" not in seen_docs:
        nav.insert(0, {"Home": "index.md"})
        seen_docs.add("index.md")

    unlisted: list[dict[str, str]] = []
    for doc in sorted(all_doc_files):
        if doc in seen_docs or is_google_verification_md(doc):
            continue
        title = page_title_from_stem(doc[:-3])
        unlisted.append({title: doc})
        audit.unlisted_pages.append(doc)

    if unlisted:
        nav.append({"Other pages": unlisted})

    return nav


def write_mkdocs_config(
    config_path: Path,
    docs_dir: Path,
    site_dir: Path,
    site_url: str,
    nav: list[object],
) -> None:
    """Write mkdocs.yml using Material theme with English + German search."""
    nav_yaml = yaml.safe_dump({"nav": nav}, allow_unicode=True, sort_keys=False)
    config_text = f"""site_name: OpenYaBS Wiki
site_description: Searchable OpenYaBS documentation synced from the GitHub wiki (English & Deutsch)
site_url: {site_url}
repo_url: https://github.com/anti43/openyabs
repo_name: anti43/openyabs
docs_dir: {docs_dir.as_posix()}
site_dir: {site_dir.as_posix()}
use_directory_urls: true

theme:
  name: material
  language: en
  font: false
  palette:
    - media: "(prefers-color-scheme: light)"
      scheme: default
      primary: blue
      accent: blue
      toggle:
        icon: material/brightness-7
        name: Switch to dark mode
    - media: "(prefers-color-scheme: dark)"
      scheme: slate
      primary: blue
      accent: blue
      toggle:
        icon: material/brightness-4
        name: Switch to light mode
  features:
    - navigation.sections
    - navigation.top
    - search.suggest
    - search.highlight
    - content.code.copy
    - toc.follow

plugins:
  - search:
      lang:
        - en
        - de

markdown_extensions:
  - tables
  - fenced_code
  - attr_list
  - toc:
      permalink: true
      slugify: !!python/object/apply:pymdownx.slugs.slugify
        kwds:
          case: lower
  - pymdownx.superfences
  - pymdownx.tilde
  - pymdownx.tasklist:
      custom_checkbox: true

extra:
  generator: false
  social:
    - icon: fontawesome/solid/globe
      link: https://anti43.github.io/openyabs/
      name: OpenYaBS Website
    - icon: fontawesome/brands/github
      link: https://github.com/anti43/openyabs
      name: GitHub Repository
    - icon: fontawesome/solid/book
      link: https://github.com/anti43/openyabs/wiki
      name: GitHub Wiki (source)
    - icon: fontawesome/solid/download
      link: https://github.com/anti43/openyabs/releases
      name: Download Releases

{nav_yaml}"""
    config_path.write_text(config_text, encoding="utf-8")


def normalize_sitemap(site_dir: Path) -> None:
    """Make sitemap.xml and sitemap.xml.gz deterministic across identical runs.

    MkDocs writes today's date in <lastmod> and a current-time gzip header,
    which would otherwise cause spurious diffs on scheduled sync runs when no
    wiki content changed.
    """
    sitemap_xml = site_dir / "sitemap.xml"
    sitemap_gz = site_dir / "sitemap.xml.gz"
    if not sitemap_xml.is_file():
        return

    raw = sitemap_xml.read_text(encoding="utf-8")
    normalized = re.sub(r"\s*<lastmod>[^<]*</lastmod>", "", raw)
    sitemap_xml.write_text(normalized, encoding="utf-8")

    with open(sitemap_gz, "wb") as raw_out:
        with gzip.GzipFile(filename="", mode="wb", fileobj=raw_out, mtime=0) as gz:
            gz.write(normalized.encode("utf-8"))


def verify_built_site(site_dir: Path, expected_assets: set[str], min_pages: int) -> dict[str, int]:
    """Fail the build if required pages, search index, or assets are missing."""
    index_html = site_dir / "index.html"
    if not index_html.is_file() or index_html.stat().st_size == 0:
        raise RuntimeError(f"Build verification failed: missing {index_html}")

    search_index = site_dir / "search" / "search_index.json"
    if not search_index.is_file() or search_index.stat().st_size == 0:
        raise RuntimeError(f"Build verification failed: missing {search_index}")

    search_data = json.loads(search_index.read_text(encoding="utf-8"))
    docs = search_data.get("docs", [])
    if len(docs) < min_pages:
        raise RuntimeError(
            f"Build verification failed: search index has only {len(docs)} entries (expected >= {min_pages})"
        )

    assets_dir = site_dir / "assets"
    if not assets_dir.is_dir() or not any(assets_dir.rglob("*.css")):
        raise RuntimeError(f"Build verification failed: missing theme CSS under {assets_dir}")

    missing_assets = [a for a in sorted(expected_assets) if not (site_dir / a).is_file()]
    if missing_assets:
        raise RuntimeError(f"Build verification failed: missing wiki assets: {missing_assets}")

    html_count = sum(1 for p in site_dir.rglob("*.html") if p.is_file())
    return {"html_files": html_count, "search_entries": len(docs)}


def safe_replace_output(staging_dir: Path, output_dir: Path) -> None:
    """Replace output_dir contents with staging_dir safely.

    Refuses to touch dangerous paths (filesystem root, repo root, or bare docs/).
    Removes stale generated wiki files inside output_dir only.
    """
    resolved = output_dir.resolve()
    if resolved.parent == resolved or resolved.name in {"", "docs", ".git"}:
        raise RuntimeError(f"Refusing to overwrite unsafe output directory: {resolved}")

    output_dir.parent.mkdir(parents=True, exist_ok=True)
    backup_dir = output_dir.parent / f".{output_dir.name}.old.{os.getpid()}"
    if backup_dir.exists():
        shutil.rmtree(backup_dir)

    had_existing = output_dir.exists()
    try:
        if had_existing:
            output_dir.rename(backup_dir)
        shutil.copytree(staging_dir, output_dir)
    except Exception:
        if output_dir.exists():
            shutil.rmtree(output_dir)
        if had_existing and backup_dir.exists():
            backup_dir.rename(output_dir)
        raise
    else:
        if backup_dir.exists():
            shutil.rmtree(backup_dir)


def main() -> int:
    parser = argparse.ArgumentParser(
        description="Build the OpenYaBS GitHub wiki into a searchable MkDocs Material site."
    )
    parser.add_argument(
        "--wiki-dir",
        required=True,
        type=Path,
        help="Path to the checked-out openyabs.wiki git repository.",
    )
    parser.add_argument(
        "--output-dir",
        required=True,
        type=Path,
        help="Destination directory for the built static site (e.g. docs/wiki).",
    )
    parser.add_argument(
        "--site-url",
        default="https://anti43.github.io/openyabs/wiki/",
        help="Published base URL (default: https://anti43.github.io/openyabs/wiki/).",
    )
    parser.add_argument(
        "--mkdocs-bin",
        default="mkdocs",
        help="Path to the mkdocs executable (default: mkdocs on PATH).",
    )
    args = parser.parse_args()

    wiki_dir: Path = args.wiki_dir.resolve()
    output_dir: Path = args.output_dir.resolve()
    site_url: str = args.site_url.rstrip("/") + "/"

    if not wiki_dir.is_dir():
        print(f"ERROR: Wiki directory does not exist: {wiki_dir}", file=sys.stderr)
        return 1

    exact_pages, lower_pages, assets = build_page_lookup(wiki_dir)
    content_md_files = sorted(
        {
            dst
            for dst in exact_pages.values()
            if not is_google_verification_md(dst)
        }
    )
    if not content_md_files:
        print(
            f"ERROR: No Markdown content pages found in {wiki_dir}; aborting to avoid publishing an empty wiki.",
            file=sys.stderr,
        )
        return 1

    audit = AuditReport()

    with tempfile.TemporaryDirectory(prefix="openyabs-wiki-build-") as tmp:
        tmp_path = Path(tmp)
        docs_dir = tmp_path / "docs"
        site_dir = tmp_path / "site"
        config_path = tmp_path / "mkdocs.yml"
        docs_dir.mkdir(parents=True, exist_ok=True)

        all_doc_files: list[str] = []

        for root, dirs, files in os.walk(wiki_dir):
            dirs[:] = sorted(d for d in dirs if d not in {".git", ".github"})
            for filename in sorted(files):
                src = Path(root) / filename
                rel = src.relative_to(wiki_dir).as_posix()
                if filename.endswith(".md"):
                    if rel in WIKI_META_FILES:
                        continue
                    dst_rel = "index.md" if rel == "Home.md" else rel
                    dst = docs_dir / dst_rel
                    dst.parent.mkdir(parents=True, exist_ok=True)
                    raw_md = src.read_text(encoding="utf-8")
                    converted = transform_markdown(
                        raw_md, rel, dst_rel, exact_pages, lower_pages, assets, audit
                    )
                    dst.write_text(converted, encoding="utf-8")
                    all_doc_files.append(dst_rel)
                    audit.pages_processed += 1
                else:
                    dst = docs_dir / rel
                    dst.parent.mkdir(parents=True, exist_ok=True)
                    shutil.copy2(src, dst)
                    audit.assets_copied += 1

        # Guarantee a home/index page even if Home.md was removed from the wiki
        if "index.md" not in all_doc_files:
            fallback_links = "\n".join(
                f"- [{page_title_from_stem(p[:-3])}](<{p}>)"
                for p in sorted(all_doc_files)
                if not is_google_verification_md(p)
            )
            (docs_dir / "index.md").write_text(
                f"# OpenYaBS Wiki\n\n{fallback_links}\n", encoding="utf-8"
            )
            all_doc_files.append("index.md")

        nav = build_navigation(wiki_dir, exact_pages, lower_pages, all_doc_files, audit)
        write_mkdocs_config(config_path, docs_dir, site_dir, site_url, nav)

        cmd = [args.mkdocs_bin, "build", "--clean", "-f", str(config_path)]
        print(f"Running: {' '.join(cmd)}")
        proc = subprocess.run(cmd, check=False)
        if proc.returncode != 0:
            print(f"ERROR: mkdocs build failed with exit code {proc.returncode}", file=sys.stderr)
            return proc.returncode

        normalize_sitemap(site_dir)
        stats = verify_built_site(site_dir, assets, min_pages=len(content_md_files))
        safe_replace_output(site_dir, output_dir)

    print("\n=== OpenYaBS Wiki Build Summary ===")
    print(f"Pages processed:          {audit.pages_processed}")
    print(f"Attachments/images:       {audit.assets_copied}")
    print(f"Internal links rewritten: {audit.internal_links_rewritten}")
    print(f"Raw wiki images localized:{audit.raw_assets_localized}")
    print(f"Wikilinks converted:      {audit.wikilinks_converted}")
    print(f"HTML pages generated:     {stats['html_files']}")
    print(f"Search index entries:     {stats['search_entries']}")
    print(f"Output directory:         {output_dir}")

    if audit.unlisted_pages:
        print("\nPages not in _Sidebar.md (added under 'Other pages'):")
        for p in audit.unlisted_pages:
            print(f"  - {p}")

    if audit.unresolved_links:
        print("\nWARNING: Unresolved internal wiki links (left unchanged):")
        for src_file, target in audit.unresolved_links:
            print(f"  - {src_file}: {target}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
