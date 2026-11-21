# OpenYaBS Wiki Pages Publisher

Builds the GitHub wiki (`https://github.com/anti43/openyabs.wiki.git`) into a searchable static documentation site under `docs/wiki/` using [MkDocs Material](https://squidfunk.github.io/mkdocs-material/) with built-in English + German full-text search.

## Why scheduled sync is used

GitHub wikis live in a separate Git repository (`anti43/openyabs.wiki.git`). Edits made in the wiki do not trigger `push` events in the main `anti43/openyabs` repository. Instead, `.github/workflows/sync-wiki.yml` checks for updates on an hourly schedule and can also be triggered on demand from the GitHub Actions tab (`workflow_dispatch`).

## Running locally

```bash
python3 -m venv .venv-wiki
.venv-wiki/bin/pip install -r tools/wiki/requirements.txt

git clone --depth 1 https://github.com/anti43/openyabs.wiki.git /tmp/openyabs.wiki
.venv-wiki/bin/python tools/wiki/build_wiki.py \
  --wiki-dir /tmp/openyabs.wiki \
  --output-dir docs/wiki \
  --mkdocs-bin .venv-wiki/bin/mkdocs
```

## What `build_wiki.py` does

- Validates that the wiki checkout contains Markdown pages before touching `docs/wiki/`.
- Copies all images and other attachments while excluding Git metadata and wiki chrome (`_Sidebar.md`, `_Footer.md`, `_Header.md`).
- Maps `Home.md` to `index.md` (`docs/wiki/index.html`) and builds navigation from `_Sidebar.md`, automatically appending any unlisted pages under **Other pages**.
- Rewrites internal GitHub wiki links (`[Page](Page-Name)`, anchors, and `[[wikilinks]]`) to MkDocs-relative `.md` links, localizes `raw.githubusercontent.com/wiki/anti43/openyabs/...` asset links to relative paths, preserves external links, and reports any unresolved internal wiki links.
- Uses GitHub-compatible Unicode heading slugs (`pymdownx.slugs.slugify(case="lower")`) so German umlaut anchors (e.g. `#leistungsdatum-und-letzte-änderung`) keep working.
- Normalizes `sitemap.xml` and `sitemap.xml.gz` so scheduled runs do not create no-op commits when the wiki has not changed.
- Builds into a staging directory, verifies `index.html`, `search/search_index.json`, theme CSS, and copied assets, and only then replaces `docs/wiki/` (removing stale files only inside `docs/wiki/`).
