import re, os, sys, zipfile, shutil, io
REPO = sys.argv[1]
OUT = os.path.join(REPO, 'src/main/resources/extra/templates')
os.makedirs(OUT, exist_ok=True)

# ---- columns of the tables (base structure + Derby updates)
inst = open(os.path.join(REPO, 'src/main/java/mpv5/db/common/DatabaseInstallation.java'), encoding='utf-8').read()
upd = open(os.path.join(REPO, 'src/main/java/mpv5/db/common/DatabaseUpdater.java'), encoding='utf-8').read()
a = inst.index('DERBY_STRUCTURE'); inst = inst[a:inst.index('};', a)]
a = upd.index('UPDATES_DERBY'); upd = upd[a:upd.index('mysql updates')]
cols = {}
def add_create(text):
    text = re.sub(r'"\s*\+\s*"', '', text)
    for m in re.finditer(r'CREATE TABLE (\w+)\s*\((.*?)\)"\s*(?:\+|,|\})', text, re.S):
        body = m.group(2)
        body = re.sub(r'"\s*\+\s*"', '', body)
        depth = 0; parts = []; cur = ''
        for ch in body:
            if ch == '(':
                depth += 1
            elif ch == ')':
                depth -= 1
            if ch == ',' and depth == 0:
                parts.append(cur); cur = ''
            else:
                cur += ch
        parts.append(cur)
        names = []
        for p in parts:
            w = p.strip().split()
            if not w:
                continue
            if w[0].upper() in ('PRIMARY', 'CONSTRAINT', 'FOREIGN', 'UNIQUE', 'CHECK', 'KEY', 'INDEX'):
                continue
            names.append(w[0].lower())
        cols.setdefault(m.group(1).lower(), [])
        for n in names:
            if n not in cols[m.group(1).lower()]:
                cols[m.group(1).lower()].append(n)
add_create(inst); add_create(upd)
for m in re.finditer(r'ALTER TABLE (\w+) ADD (?:COLUMN )?(\w+)', upd, re.I):
    t, c = m.group(1).lower(), m.group(2).lower()
    cols.setdefault(t, [])
    if c not in cols[t]:
        cols[t].append(c)
SKIP = {'ids', 'intaddedby', 'reserve1', 'reserve2', 'password', 'invisible', 'hierarchypath', 'defaults', 'data'}
def fields(table, prefix):
    return [prefix + c for c in cols.get(table, []) if c not in SKIP]

# formatted helpers known from the bundled template / docs
ITEM_EXTRA = ['item.netvaluef', 'item.taxvaluef', 'item.grosvaluef', 'item.discountvaluef', 'item.discountnetdifferencef',
              'item.contact.fullname', 'item.contact.invoiceaddress.street', 'item.contact.invoiceaddress.zip',
              'item.contact.invoiceaddress.city', 'item.contact.invoiceaddress.country',
              'item.paymentiban', 'item.paymentbic', 'item.paymentbank', 'item.paymentaccountname', 'item.paymentdays',
              'item.paymentduedate', 'item.paymentterms', 'item.skontopercent', 'item.skontodays', 'item.skontodate',
              'item.skontoamount', 'item.skontopayamount', 'group', 'addedby', 'addedbyfull']
COMPANY = ['companyinfo.' + k for k in ['business', 'firstname', 'name', 'street', 'zip', 'city', 'country', 'phoneprefix', 'phone',
           'fax', 'email', 'account', 'banknumber', 'bankname', 'account1', 'taxnumber', 'taxauthority', 'taxmandant',
           'taxadvsior', 'taxadvjob', 'payterm', 'sepacreditorid']]

# ---- the base template
base = os.path.join(sys.argv[2])
z = zipfile.ZipFile(os.path.join(REPO, 'src/main/resources/extra/DE-Yabs-2.0.odt'))
files = {n: z.read(n) for n in z.namelist()}
content = files['content.xml'].decode('utf-8')
manifest = files['META-INF/manifest.xml'].decode('utf-8')

ph = lambda n: '<text:placeholder text:placeholder-type="text">&lt;%s&gt;</text:placeholder>' % n
def esc(s): return s.replace('&', '&amp;').replace('<', '&lt;').replace('>', '&gt;')

STYLES = ('<style:style style:name="PBreak" style:family="paragraph" style:parent-style-name="Standard"><style:paragraph-properties fo:break-before="page"/>'
          '<style:text-properties fo:font-size="13pt" fo:font-weight="bold"/></style:style>'
          '<style:style style:name="PRefHint" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties fo:font-size="8pt" fo:color="#666666"/></style:style>'
          '<style:style style:name="PRefKey" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties style:font-name="Courier New" fo:font-size="7.5pt" fo:color="#444444"/></style:style>'
          '<style:style style:name="PRefVal" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties fo:font-size="8pt"/></style:style>'
          '<style:style style:name="RefT" style:family="table"><style:table-properties style:width="17cm" table:align="left"/></style:style>'
          '<style:style style:name="RefT.A" style:family="table-column"><style:table-column-properties style:column-width="7cm"/></style:style>'
          '<style:style style:name="RefT.B" style:family="table-column"><style:table-column-properties style:column-width="10cm"/></style:style>'
          '<style:style style:name="RefT.C" style:family="table-cell"><style:table-cell-properties fo:padding="0.04cm" fo:border-bottom="0.5pt solid #dddddd" fo:border-top="none" fo:border-left="none" fo:border-right="none"/></style:style>'
          '<style:style style:name="frLogo" style:family="graphic" style:parent-style-name="Graphics"><style:graphic-properties style:vertical-pos="top" style:vertical-rel="baseline" fo:padding="0cm" fo:border="none"/></style:style>')

def reference(title, groups):
    x = ['<text:p text:style-name="PBreak">%s</text:p>' % esc(title),
         '<text:p text:style-name="PRefHint">%s</text:p>' % esc('Alle Felder dieser Vorlage: links der Name, rechts der Wert beim Export. Felder in eigene Vorlagen kopieren (Einfügen › Feldbefehl › Platzhalter) und diese Seite löschen.')]
    for gname, flist in groups:
        if not flist:
            continue
        x.append('<text:p text:style-name="P5"><text:span text:style-name="T6">%s</text:span></text:p>' % esc(gname))
        x.append('<table:table table:name="Felder_%d" table:style-name="RefT"><table:table-column table:style-name="RefT.A"/><table:table-column table:style-name="RefT.B"/>' % (abs(hash(gname)) % 100000))
        for f in flist:
            x.append('<table:table-row><table:table-cell table:style-name="RefT.C" office:value-type="string"><text:p text:style-name="PRefKey">%s</text:p></table:table-cell>'
                     '<table:table-cell table:style-name="RefT.C" office:value-type="string"><text:p text:style-name="PRefVal">%s</text:p></table:table-cell></table:table-row>' % (esc(f), ph(f)))
        x.append('</table:table>')
    return ''.join(x)

LOGO = ('<text:p text:style-name="P23"><draw:frame draw:style-name="frLogo" draw:name="Logo" text:anchor-type="as-char" svg:width="4.2cm" svg:height="1.4cm" draw:z-index="3">'
        '<draw:image xlink:href="Pictures/logo.png" xlink:type="simple" xlink:show="embed" xlink:actuate="onLoad"/></draw:frame></text:p>')

def make(fname, title, kind, groups, prefix=None, cprefix=None):
    c = content
    if prefix:
        c = c.replace('&lt;item.contact.', '&lt;' + cprefix).replace('&lt;item.', '&lt;' + prefix)
    c = c.replace('</office:automatic-styles>', STYLES + '</office:automatic-styles>', 1)
    assert '<text:p text:style-name="P23">Firmenlogo</text:p>' in c
    c = c.replace('<text:p text:style-name="P23">Firmenlogo</text:p>', LOGO, 1)
    c = c.replace('<text:span text:style-name="T6">Rechnung </text:span>', '<text:span text:style-name="T6">%s </text:span>' % esc(title), 1)
    end = c.index('</office:text>')
    if kind == 'item':
        body_end = c[:end]
    else:
        # keep the head (logo, addresses, title line); replace the positions and totals
        cut = c.index('<text:p text:style-name="P7"/><table:table table:name="Tabelle1"')
        body_end = c[:cut]
        if kind in ('positions',):
            # the position table (xtable) and the totals stay
            body_end = c[:end]
    c = body_end + reference('Verfügbare Felder – ' + title, groups) + c[end:]
    out = io.BytesIO()
    zo = zipfile.ZipFile(out, 'w')
    zo.writestr(zipfile.ZipInfo('mimetype'), files['mimetype'], compress_type=zipfile.ZIP_STORED)
    man = manifest
    if 'Pictures/logo.png' not in man:
        man = man.replace('</manifest:manifest>', ' <manifest:file-entry manifest:media-type="image/png" manifest:full-path="Pictures/logo.png"/>\n</manifest:manifest>')
    for n, d in files.items():
        if n in ('mimetype',):
            continue
        if n == 'content.xml':
            d = c.encode('utf-8')
        elif n == 'META-INF/manifest.xml':
            d = man.encode('utf-8')
        zo.writestr(n, d, compress_type=zipfile.ZIP_DEFLATED)
    zo.writestr('Pictures/logo.png', open(os.path.join(os.path.dirname(__file__), 'logo.png'), 'rb').read(), compress_type=zipfile.ZIP_STORED)
    zo.close()
    data = out.getvalue()
    open(os.path.join(OUT, fname), 'wb').write(data)
    import xml.dom.minidom
    xml.dom.minidom.parseString(c.encode('utf-8'))
    return len(data)

item = fields('items', 'item.') + ITEM_EXTRA
contact_of = lambda p: fields('contacts', p)
positions = ['item.xtable1 (Positionstabelle: Tabelle mit dem Namen xtable1)']
TYPES = [
    (0, 'Rechnung', 'item'), (2, 'Angebot', 'item'), (1, 'Auftrag', 'item'), (3, 'Lieferschein', 'item'),
    (4, 'Auftragsbestätigung', 'item'), (20, 'Gutschrift', 'item'), (21, 'Abschlagsrechnung', 'item'),
    (22, 'Anzahlungsrechnung', 'item'), (11, 'Vertrag', 'item'), (13, 'Serienbrief', 'item'),
]
made = []
for tid, title, kind in TYPES:
    g = [('Beleg', item), ('Kontakt des Belegs', contact_of('item.contact.')), ('Firma', COMPANY)]
    made.append((tid, title, 'demo-%02d.odt' % tid, make('demo-%02d.odt' % tid, title, kind, g)))
OTHERS = [
    (7, 'Mahnung', 'other', [('Mahnung', fields('reminders', 'reminder.')), ('Beleg', item), ('Firma', COMPANY)], None, None),
    (8, 'Kontaktdatenblatt', 'other', [('Kontakt', contact_of('contact.')), ('Firma', COMPANY)], 'contact.', 'contact.'),
    (5, 'Artikeldatenblatt', 'other', [('Artikel', fields('products', 'product.')), ('Firma', COMPANY)], 'product.', 'product.'),
    (6, 'Leistungsbeschreibung', 'other', [('Dienstleistung', fields('products', 'product.')), ('Firma', COMPANY)], 'product.', 'product.'),
    (10, 'Bestellung', 'positions', [('Bestellung', fields('productorders', 'productorder.')), ('Lieferant', contact_of('productorder.contact.')), ('Firma', COMPANY)], 'productorder.', 'productorder.contact.'),
    (12, 'Gesprächsnotiz', 'other', [('Gespräch', fields('conversations', 'conversation.')), ('Kontakt', contact_of('conversation.contact.')), ('Firma', COMPANY)], 'conversation.', 'conversation.contact.'),
    (14, 'Tätigkeitsliste', 'positions', [('Tätigkeitsliste', fields('activitylists', 'activitylist.')), ('Kontakt', contact_of('activitylist.contact.')), ('Firma', COMPANY)], 'activitylist.', 'activitylist.contact.'),
    (9, 'Journal', 'positions', [('Firma', COMPANY)], None, None),
]
for tid, title, kind, g, p, cp in OTHERS:
    made.append((tid, title, 'demo-%02d.odt' % tid, make('demo-%02d.odt' % tid, title, kind, g, p, cp)))
for m in made:
    print(m)
print({t: len(cols.get(t, [])) for t in ['items', 'contacts', 'products', 'reminders', 'productorders', 'conversations', 'activitylists']})
