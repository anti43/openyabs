import re, os, sys, zipfile, shutil, io, zlib
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
        x.append('<table:table table:name="Felder_%d" table:style-name="RefT"><table:table-column table:style-name="RefT.A"/><table:table-column table:style-name="RefT.B"/>' % (zlib.crc32(gname.encode("utf-8")) % 100000))
        for f in flist:
            x.append('<table:table-row><table:table-cell table:style-name="RefT.C" office:value-type="string"><text:p text:style-name="PRefKey">%s</text:p></table:table-cell>'
                     '<table:table-cell table:style-name="RefT.C" office:value-type="string"><text:p text:style-name="PRefVal">%s</text:p></table:table-cell></table:table-row>' % (esc(f), ph(f)))
        x.append('</table:table>')
    return ''.join(x)

LOGO = ('<text:p text:style-name="P23"><draw:frame draw:style-name="frLogo" draw:name="Logo" text:anchor-type="as-char" svg:width="4.2cm" svg:height="1.4cm" draw:z-index="3">'
        '<draw:image xlink:href="Pictures/logo.png" xlink:type="simple" xlink:show="embed" xlink:actuate="onLoad"/></draw:frame></text:p>')

# ---- the journal: landscape, the journal table (xtable1, columns from the template
# format: date, number, type, name, account, status, net, tax, gross, profit) and the totals
JOURNAL_FORMAT = '6,9,10,1,8,11,12,13,14,15'  # used by FirstStartSetup (columns of the journal row without the first)
JOURNAL_COLS = [('Datum', 2.3, False), ('Nummer', 2.3, False), ('Typ', 2.6, False), ('Name', 4.8, False),
                ('Konto', 3.2, False), ('Status', 2.3, False), ('Netto', 2.3, True), ('Steuer', 2.2, True),
                ('Brutto', 2.3, True), ('Gewinn', 2.4, True)]
JOURNAL_FIELDS = ['journal.datefrom', 'journal.dateto', 'journal.datenow', 'journal.group', 'journal.childgroups',
                  'journal.user', 'journal.accounts', 'journal.netvalue', 'journal.taxvalue', 'journal.grosvalue',
                  'journal.revenuevalue', 'journal.revenuetax', 'journal.revenuegrosvalue', 'journal.expensevalue',
                  'journal.expensetax', 'journal.expensegrosvalue', 'journal.profitvalue', 'journal.profittax',
                  'journal.discountvalue', 'Journal.xtable1 (Tabelle mit dem Namen xtable1)']
JSTYLES = ('<style:style style:name="JTitle" style:family="paragraph" style:parent-style-name="Standard"><style:paragraph-properties fo:margin-top="0.2cm" fo:margin-bottom="0.2cm"/><style:text-properties style:font-name="Arial" fo:font-size="16pt" fo:font-weight="bold"/></style:style>'
           '<style:style style:name="JLogo" style:family="paragraph" style:parent-style-name="Standard"><style:paragraph-properties fo:text-align="end"/></style:style>'
           '<style:style style:name="JInfoK" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties style:font-name="Arial" fo:font-size="9pt" fo:color="#555555"/></style:style>'
           '<style:style style:name="JInfoV" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties style:font-name="Arial" fo:font-size="9pt"/></style:style>'
           '<style:style style:name="JHeadL" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties style:font-name="Arial" fo:font-size="8.5pt" fo:font-weight="bold"/></style:style>'
           '<style:style style:name="JHeadR" style:family="paragraph" style:parent-style-name="Standard"><style:paragraph-properties fo:text-align="end"/><style:text-properties style:font-name="Arial" fo:font-size="8.5pt" fo:font-weight="bold"/></style:style>'
           '<style:style style:name="JCellL" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties style:font-name="Arial" fo:font-size="8.5pt"/></style:style>'
           '<style:style style:name="JCellR" style:family="paragraph" style:parent-style-name="Standard"><style:paragraph-properties fo:text-align="end"/><style:text-properties style:font-name="Arial" fo:font-size="8.5pt"/></style:style>'
           '<style:style style:name="JSumK" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties style:font-name="Arial" fo:font-size="9pt" fo:color="#555555"/></style:style>'
           '<style:style style:name="JSumV" style:family="paragraph" style:parent-style-name="Standard"><style:paragraph-properties fo:text-align="end" fo:margin-right="1cm"/><style:text-properties style:font-name="Arial" fo:font-size="9pt" fo:font-weight="bold"/></style:style>'
           '<style:style style:name="JSumH" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties style:font-name="Arial" fo:font-size="9.5pt" fo:font-weight="bold"/></style:style>'
           '<style:style style:name="JGap" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties fo:font-size="6pt"/></style:style>'
           '<style:style style:name="JT" style:family="table"><style:table-properties style:width="26.7cm" table:align="left"/></style:style>'
           '<style:style style:name="JInfo" style:family="table"><style:table-properties style:width="14cm" table:align="left"/></style:style>'
           '<style:style style:name="JInfo.A" style:family="table-column"><style:table-column-properties style:column-width="3cm"/></style:style>'
           '<style:style style:name="JInfo.B" style:family="table-column"><style:table-column-properties style:column-width="11cm"/></style:style>'
           '<style:style style:name="JHeadC" style:family="table-cell"><style:table-cell-properties fo:padding="0.06cm" fo:background-color="#eeeeee" fo:border-bottom="0.75pt solid #555555" fo:border-top="none" fo:border-left="none" fo:border-right="none"/></style:style>'
           '<style:style style:name="JRowC" style:family="table-cell"><style:table-cell-properties fo:padding="0.06cm" fo:border-bottom="0.5pt solid #dddddd" fo:border-top="none" fo:border-left="none" fo:border-right="none"/></style:style>'
           '<style:style style:name="JPlainC" style:family="table-cell"><style:table-cell-properties fo:padding="0.03cm" fo:border="none"/></style:style>'
           '<style:style style:name="JSumT" style:family="table"><style:table-properties style:width="26.7cm" fo:margin-top="0.4cm" table:align="left"/></style:style>'
           '<style:style style:name="JSumT.K" style:family="table-column"><style:table-column-properties style:column-width="3.675cm"/></style:style>'
           '<style:style style:name="JSumT.V" style:family="table-column"><style:table-column-properties style:column-width="3cm"/></style:style>'
           + ''.join('<style:style style:name="JT.%d" style:family="table-column"><style:table-column-properties style:column-width="%.2fcm"/></style:style>' % (i, w)
                     for i, (n, w, r) in enumerate(JOURNAL_COLS)))

def journal(c):
    """the content of the journal: own body, landscape page"""
    c = c.replace('</office:automatic-styles>', JSTYLES + '</office:automatic-styles>', 1)
    a = c.index('</text:sequence-decls>') + len('</text:sequence-decls>')
    end = c.index('</office:text>')
    cell = lambda style, para, text='': '<table:table-cell table:style-name="%s" office:value-type="string"><text:p text:style-name="%s">%s</text:p></table:table-cell>' % (style, para, text)
    cols = ''.join('<table:table-column table:style-name="JT.%d"/>' % i for i in range(len(JOURNAL_COLS)))
    x = [LOGO.replace('text:style-name="P23"', 'text:style-name="JLogo"'),
         '<text:p text:style-name="JTitle">Journal</text:p>',
         '<table:table table:name="Info" table:style-name="JInfo"><table:table-column table:style-name="JInfo.A"/><table:table-column table:style-name="JInfo.B"/>']
    for k, v in [('Zeitraum', ph('journal.datefrom') + ' – ' + ph('journal.dateto')), ('Gruppe', ph('journal.group')),
                 ('Benutzer', ph('journal.user')), ('Konten', ph('journal.accounts')), ('Erstellt am', ph('journal.datenow'))]:
        x.append('<table:table-row>' + cell('JPlainC', 'JInfoK', esc(k)) + cell('JPlainC', 'JInfoV', v) + '</table:table-row>')
    x.append('</table:table><text:p text:style-name="JGap"/>')
    # the header and the data table: same columns; YaBS fills the first row of xtable1 once per journal line
    x.append('<table:table table:name="JournalKopf" table:style-name="JT">' + cols + '<table:table-row>'
             + ''.join(cell('JHeadC', 'JHeadR' if r else 'JHeadL', esc(n)) for n, w, r in JOURNAL_COLS) + '</table:table-row></table:table>')
    x.append('<table:table table:name="xtable1" table:style-name="JT">' + cols + '<table:table-row>'
             + ''.join(cell('JRowC', 'JCellR' if r else 'JCellL') for n, w, r in JOURNAL_COLS) + '</table:table-row></table:table>')
    # the totals: four blocks like in the journal panel
    blocks = [('Gesamt', [('Netto', 'journal.netvalue'), ('Steuer', 'journal.taxvalue'), ('Brutto', 'journal.grosvalue')]),
              ('Einnahmen (bezahlt)', [('Netto', 'journal.revenuevalue'), ('Steuer', 'journal.revenuetax'), ('Brutto', 'journal.revenuegrosvalue')]),
              ('Ausgaben (bezahlt)', [('Netto', 'journal.expensevalue'), ('Steuer', 'journal.expensetax'), ('Brutto', 'journal.expensegrosvalue')]),
              ('Gewinn', [('Netto', 'journal.profitvalue'), ('Steuer', 'journal.profittax'), ('Rabatt', 'journal.discountvalue')])]
    x.append('<table:table table:name="Summen" table:style-name="JSumT">' + '<table:table-column table:style-name="JSumT.K"/><table:table-column table:style-name="JSumT.V"/>' * 4)
    x.append('<table:table-row>' + ''.join(cell('JPlainC', 'JSumH', esc(b)) + cell('JPlainC', 'JSumH') for b, r in blocks) + '</table:table-row>')
    for i in range(3):
        x.append('<table:table-row>' + ''.join(cell('JPlainC', 'JSumK', esc(r[i][0])) + cell('JPlainC', 'JSumV', ph(r[i][1])) for b, r in blocks) + '</table:table-row>')
    x.append('</table:table>')
    c = c[:a] + ''.join(x) + c[end:]
    st = files['styles.xml'].decode('utf-8')
    old = re.search(r'(<style:page-layout style:name="Mpm1">\s*<style:page-layout-properties )([^>]*)>', st)
    props = re.sub(r'fo:page-width="[^"]*"', 'fo:page-width="29.7cm"', old.group(2))
    props = re.sub(r'fo:page-height="[^"]*"', 'fo:page-height="21cm"', props)
    props = props.replace('style:print-orientation="portrait"', 'style:print-orientation="landscape"')
    props = re.sub(r'fo:margin-(top|bottom|left|right)="[^"]*"', lambda m: 'fo:margin-%s="1.5cm"' % m.group(1), props)
    st = st[:old.start()] + old.group(1) + props + '>' + st[old.end():]
    return c, st

# the body of the documents without positions: a table of label/value rows and text paragraphs
BSTYLES = ('<style:style style:name="BT" style:family="table"><style:table-properties style:width="16cm" fo:margin-top="0.3cm" table:align="left"/></style:style>'
           '<style:style style:name="BT.A" style:family="table-column"><style:table-column-properties style:column-width="4.5cm"/></style:style>'
           '<style:style style:name="BT.B" style:family="table-column"><style:table-column-properties style:column-width="11.5cm"/></style:style>'
           '<style:style style:name="BT.C" style:family="table-cell"><style:table-cell-properties fo:padding="0.07cm" fo:border-bottom="0.5pt solid #dddddd" fo:border-top="none" fo:border-left="none" fo:border-right="none"/></style:style>'
           '<style:style style:name="BK" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties style:font-name="Arial" fo:font-size="10pt" fo:color="#555555"/></style:style>'
           '<style:style style:name="BV" style:family="paragraph" style:parent-style-name="Standard"><style:text-properties style:font-name="Arial" fo:font-size="10pt"/></style:style>'
           '<style:style style:name="BP" style:family="paragraph" style:parent-style-name="Standard"><style:paragraph-properties fo:margin-top="0.25cm" fo:margin-bottom="0.15cm"/><style:text-properties style:font-name="Arial" fo:font-size="10pt"/></style:style>'
           '<style:style style:name="BH" style:family="paragraph" style:parent-style-name="Standard"><style:paragraph-properties fo:margin-top="0.4cm"/><style:text-properties style:font-name="Arial" fo:font-size="10.5pt" fo:font-weight="bold"/></style:style>')

def fill(text):
    """a text with fields written as {field}"""
    return re.sub(r'\{([a-z.]+)\}', lambda m: ph(m.group(1)), esc(text).replace('{', '{'))

def body(blocks):
    x = []
    n = 0
    for kind, data in blocks:
        if kind == 'p':
            x.append('<text:p text:style-name="BP">%s</text:p>' % fill(data))
        elif kind == 'h':
            x.append('<text:p text:style-name="BH">%s</text:p>' % esc(data))
        elif kind == 't':
            n += 1
            x.append('<table:table table:name="Daten%d" table:style-name="BT"><table:table-column table:style-name="BT.A"/><table:table-column table:style-name="BT.B"/>' % n)
            for k, v in data:
                x.append('<table:table-row><table:table-cell table:style-name="BT.C" office:value-type="string"><text:p text:style-name="BK">%s</text:p></table:table-cell>'
                         '<table:table-cell table:style-name="BT.C" office:value-type="string"><text:p text:style-name="BV">%s</text:p></table:table-cell></table:table-row>' % (esc(k), fill(v)))
            x.append('</table:table>')
    return ''.join(x)

SENDER = ('<text:span text:style-name="T7">Firma · Musterstrasse <text:s/>· D-66663 Musterheim</text:span>',
          '<text:span text:style-name="T7">%s · %s · %s %s</text:span>' % (ph('companyinfo.name'), ph('companyinfo.street'), ph('companyinfo.zip'), ph('companyinfo.city')))
DUPLICATE_NAME = re.compile(r'<text:p text:style-name="P12"><text:span text:style-name="T5">\(</text:span>.*?<text:span text:style-name="T5">\)</text:span></text:p>')
TOTALS = re.compile(r'<table:table table:name="Tabelle3".*?</table:table>')

def make(fname, title, kind, groups, prefix=None, cprefix=None, blocks=None, total_label=None, date_field=None, addressee=True, totals=None):
    c = content
    styles_xml = None
    if kind == 'journal':
        c, styles_xml = journal(c)
    else:
        # the sender line from the company information; the name only once
        assert SENDER[0] in c
        c = c.replace(SENDER[0], SENDER[1], 1)
        c, n = DUPLICATE_NAME.subn('', c, 1)
        assert n == 1
        if date_field:
            c = c.replace('&lt;item.dateadded&gt;', '&lt;%s&gt;' % date_field, 1)
        if not addressee:
            # products, services: nobody to address
            c = re.sub(r'<text:placeholder text:placeholder-type="text">&lt;item\.contact\.[a-z.]*&gt;</text:placeholder>', '', c)
            c = c.replace('<text:p text:style-name="P4">Kundennummer: </text:p>', '<text:p text:style-name="P4"/>', 1)
        if total_label:
            assert '>Rechnungsbetrag<' in c
            c = c.replace('>Rechnungsbetrag<', '>%s<' % esc(total_label), 1)
        if totals is not None:
            c = TOTALS.sub(lambda m: body(totals), c, 1)
    if prefix:
        c = c.replace('&lt;item.contact.', '&lt;' + cprefix).replace('&lt;item.', '&lt;' + prefix)
    c = c.replace('</office:automatic-styles>', STYLES + BSTYLES + '</office:automatic-styles>', 1)
    assert kind == 'journal' or '<text:p text:style-name="P23">Firmenlogo</text:p>' in c
    c = c.replace('<text:p text:style-name="P23">Firmenlogo</text:p>', LOGO, 1)
    c = c.replace('<text:span text:style-name="T6">Rechnung </text:span>', '<text:span text:style-name="T6">%s </text:span>' % esc(title), 1)
    end = c.index('</office:text>')
    if kind in ('item', 'journal', 'positions'):
        body_end = c[:end]
    else:
        # keep the head (logo, addresses, title line); the positions and totals are replaced by the body
        cut = c.index('<text:p text:style-name="P7"/><table:table table:name="Tabelle1"')
        body_end = c[:cut]
    c = body_end + body(blocks or []) + reference('Verfügbare Felder – ' + title, groups) + c[end:]
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
        elif n == 'styles.xml' and styles_xml is not None:
            d = styles_xml.encode('utf-8')
        elif n == 'META-INF/manifest.xml':
            d = man.encode('utf-8')
        zo.writestr(zipfile.ZipInfo(n, (2026, 1, 1, 0, 0, 0)), d, compress_type=zipfile.ZIP_DEFLATED)
    zo.writestr(zipfile.ZipInfo('Pictures/logo.png', (2026, 1, 1, 0, 0, 0)), open(os.path.join(os.path.dirname(__file__), 'logo.png'), 'rb').read(), compress_type=zipfile.ZIP_STORED)
    zo.close()
    data = out.getvalue()
    open(os.path.join(OUT, fname), 'wb').write(data)
    import xml.dom.minidom
    xml.dom.minidom.parseString(c.encode('utf-8'))
    return len(data)

item = fields('items', 'item.') + ITEM_EXTRA
contact_of = lambda p: fields('contacts', p)
positions = ['item.xtable1 (Positionstabelle: Tabelle mit dem Namen xtable1)']
# type, title, label of the total
TYPES = [
    (0, 'Rechnung', 'Rechnungsbetrag'), (2, 'Angebot', 'Angebotssumme'), (1, 'Auftrag', 'Auftragssumme'),
    (3, 'Lieferschein', 'Gesamtbetrag'), (4, 'Auftragsbestätigung', 'Auftragssumme'), (20, 'Gutschrift', 'Gutschriftsbetrag'),
    (21, 'Abschlagsrechnung', 'Rechnungsbetrag'), (22, 'Anzahlungsrechnung', 'Rechnungsbetrag'), (11, 'Vertrag', 'Gesamtbetrag'),
]
made = []
for tid, title, total in TYPES:
    g = [('Beleg', item + positions), ('Kontakt des Belegs', contact_of('item.contact.')), ('Firma', COMPANY)]
    made.append((tid, title, 'demo-%02d.odt' % tid, make('demo-%02d.odt' % tid, title, 'item', g, total_label=total)))

R = 'reminder.item.'
REMINDER = [('p', 'Sehr geehrte Damen und Herren,'),
            ('p', 'zu unserer Rechnung Nr. {reminder.item.cnumber} vom {reminder.item.dateadded} über {reminder.item.grosvaluef} konnten wir bis heute keinen Zahlungseingang feststellen.'),
            ('t', [('Rechnungsnummer', '{reminder.item.cnumber}'), ('Rechnungsdatum', '{reminder.item.dateadded}'),
                   ('Fällig seit', '{reminder.item.paymentduedate}'), ('Offener Betrag', '{reminder.item.grosvaluef}'),
                   ('Mahngebühr', '{reminder.extravalue}'), ('Mahnstufe', '{reminder.stage}')]),
            ('p', '{reminder.description}'),
            ('p', 'Bitte überweisen Sie den offenen Betrag innerhalb von 7 Tagen auf unser Konto: IBAN {reminder.item.paymentiban}, BIC {reminder.item.paymentbic}, {reminder.item.paymentbank}. Sollten Sie inzwischen gezahlt haben, betrachten Sie dieses Schreiben bitte als gegenstandslos.'),
            ('p', 'Mit freundlichen Grüßen'), ('p', '{companyinfo.name}')]
CONTACT = [('t', [('Kundennummer', '{contact.cnumber}'), ('Firma', '{contact.company}'), ('Anrede', '{contact.title}'),
                  ('Vorname', '{contact.prename}'), ('Name', '{contact.cname}'), ('Straße', '{contact.street}'),
                  ('PLZ / Ort', '{contact.zip} {contact.city}'), ('Land', '{contact.country}'), ('Telefon', '{contact.mainphone}'),
                  ('Mobil', '{contact.mobilephone}'), ('Fax', '{contact.fax}'), ('E-Mail', '{contact.mailaddress}'),
                  ('Website', '{contact.website}'), ('Steuernummer', '{contact.taxnumber}'), ('Bank', '{contact.bankname}'),
                  ('IBAN', '{contact.bankaccount}'), ('BIC', '{contact.bankid}')]),
           ('h', 'Notizen'), ('p', '{contact.notes}')]
PRODUCT = [('t', [('Artikelnummer', '{product.cnumber}'), ('Bezeichnung', '{product.cname}'), ('Beschreibung', '{product.description}'),
                  ('Einheit', '{product.measure}'), ('Preis netto', '{product.externalnetvalue}'), ('Steuersatz', '{product.taxpercentvalue}'),
                  ('Preis brutto', '{product.externalbrutvalue}'), ('EAN', '{product.ean}'), ('Referenz', '{product.reference}'),
                  ('Warengruppe', '{product.productgroup.cname}'), ('Hersteller', '{product.manufacturer}'), ('Lieferant', '{product.supplier}'),
                  ('Link', '{product.url}')])]
SERVICE = [('t', [('Leistungsnummer', '{product.cnumber}'), ('Bezeichnung', '{product.cname}'), ('Einheit', '{product.measure}'),
                  ('Preis netto', '{product.externalnetvalue}'), ('Steuersatz', '{product.taxpercentvalue}'),
                  ('Preis brutto', '{product.externalbrutvalue}'), ('Warengruppe', '{product.productgroup.cname}')]),
           ('h', 'Beschreibung'), ('p', '{product.description}')]
CONVERSATION = [('t', [('Datum', '{conversation.date}'), ('Betreff', '{conversation.cname}'),
                       ('Gesprächspartner', '{conversation.contact.fullname}'), ('Telefon', '{conversation.contact.mainphone}')]),
                ('h', 'Inhalt'), ('p', '{conversation.content}')]
ACTIVITY_TOTAL = [('t', [('Gesamtbetrag (brutto)', '{item.grosvaluef}')])]
# mass print (Serienbrief): a letter to each chosen contact with the text written in the mass print view
MASSPRINT = [('p', '{massprint.intro} {contact.fullname},'), ('p', '{massprint.content}'),
             ('p', 'Mit freundlichen Grüßen'), ('p', '{companyinfo.name}')]
massprint_fields = ['massprint.' + k for k in ['cname', 'intro', 'gender', 'content', 'dateadded', 'addedby']]
reminder_fields = [f for f in fields('reminders', 'reminder.') if f != 'reminder.itemsids' and f != 'reminder.stagesids'] + ['reminder.stage', 'reminder.count']
OTHERS = [
    (7, 'Mahnung zu Rechnung', 'other', [('Mahnung', reminder_fields), ('Rechnung', fields('items', R) + [R + 'grosvaluef', R + 'paymentduedate', R + 'paymentiban']), ('Firma', COMPANY)],
     dict(prefix=R, cprefix=R + 'contact.', blocks=REMINDER, date_field='reminder.dateadded')),
    (8, 'Kontaktdatenblatt', 'other', [('Kontakt', contact_of('contact.')), ('Firma', COMPANY)], dict(prefix='contact.', cprefix='contact.', blocks=CONTACT)),
    (5, 'Artikeldatenblatt', 'other', [('Artikel', fields('products', 'product.')), ('Firma', COMPANY)], dict(prefix='product.', cprefix='product.', blocks=PRODUCT, addressee=False)),
    (6, 'Leistungsbeschreibung', 'other', [('Dienstleistung', fields('products', 'product.')), ('Firma', COMPANY)], dict(prefix='product.', cprefix='product.', blocks=SERVICE, addressee=False)),
    (10, 'Bestellung', 'positions', [('Bestellung', fields('productorders', 'productorder.') + ['productorder.xtable1']), ('Lieferant', contact_of('productorder.contact.')), ('Firma', COMPANY)],
     dict(prefix='productorder.', cprefix='productorder.contact.', total_label='Bestellsumme')),
    (12, 'Gesprächsnotiz', 'other', [('Gespräch', fields('conversations', 'conversation.')), ('Kontakt', contact_of('conversation.contact.')), ('Firma', COMPANY)],
     dict(prefix='conversation.', cprefix='conversation.contact.', blocks=CONVERSATION)),
    (14, 'Tätigkeitsliste', 'positions', [('Tätigkeitsliste', fields('activitylists', 'activitylist.') + ['activitylist.grosvaluef', 'activitylist.xtable1']), ('Kontakt', contact_of('activitylist.contact.')), ('Firma', COMPANY)],
     dict(prefix='activitylist.', cprefix='activitylist.contact.', totals=ACTIVITY_TOTAL)),
    (9, 'Journal', 'journal', [('Journal', JOURNAL_FIELDS), ('Firma', COMPANY)], {}),
    (13, 'Serienbrief', 'other', [('Serienbrief', massprint_fields), ('Kontakt', contact_of('contact.')), ('Firma', COMPANY)],
     dict(prefix='contact.', cprefix='contact.', blocks=MASSPRINT, date_field='massprint.dateadded')),
]
for tid, title, kind, g, kw in OTHERS:
    made.append((tid, title, 'demo-%02d.odt' % tid, make('demo-%02d.odt' % tid, title, kind, g, **kw)))
# the regular invoice template (imported on first start): the base with the sender line from the
# company information and the name only once
def write_odt(fname, c):
    out = io.BytesIO()
    zo = zipfile.ZipFile(out, 'w')
    zo.writestr(zipfile.ZipInfo('mimetype'), files['mimetype'], compress_type=zipfile.ZIP_STORED)
    for n, d in files.items():
        if n == 'mimetype':
            continue
        if n == 'content.xml':
            d = c.encode('utf-8')
        zo.writestr(zipfile.ZipInfo(n, (2026, 1, 1, 0, 0, 0)), d, compress_type=zipfile.ZIP_DEFLATED)
    zo.close()
    open(os.path.join(OUT, fname), 'wb').write(out.getvalue())
    return len(out.getvalue())
inv = content.replace(SENDER[0], SENDER[1], 1)
inv, n = DUPLICATE_NAME.subn('', inv, 1)
assert n == 1
made.append((0, 'Rechnung (Standard)', 'invoice.odt', write_odt('invoice.odt', inv)))
for m in made:
    print(m)
print({t: len(cols.get(t, [])) for t in ['items', 'contacts', 'products', 'reminders', 'productorders', 'conversations', 'activitylists']})
