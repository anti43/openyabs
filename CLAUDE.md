# Notes for working on this repository

## Documentation

- Documentation goes to the **GitHub wiki** (https://github.com/anti43/openyabs/wiki, git: `https://github.com/anti43/openyabs.wiki.git`), **not** into `docs/*.md`.
- Keep German and English separate, as in the wiki: German pages without suffix (e.g. `Datenbank`, `Kasse-und-TSE`), English pages with the suffix `-(EN)` (e.g. `Database-(EN)`, `Cash-register-and-TSE-(EN)`). Each page links to its counterpart in the other language.
- New pages go into the entry pages `Openyabs-Dokumentation-(DE)` / `Openyabs-documentation-(EN)` and into `_Sidebar`. Images go to `images/` in the wiki and are linked as `https://raw.githubusercontent.com/wiki/anti43/openyabs/images/<file>`.
- Links from the app, `README.md` and the website point to wiki pages (`https://github.com/anti43/openyabs/wiki/<Page>`).
- `docs/` holds only the website (GitHub Pages: `index.html`, the `*.html` redirect pages to the wiki, `.nojekyll`, the Google verification file) and the screenshots in `docs/images` used by README and website.
- If the wiki cannot be pushed from the session, prepare the wiki commit as a patch (`git format-patch`) for the maintainer to apply with `git am` and push.
