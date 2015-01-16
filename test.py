#!/usr/bin/env python3

import os

__author__ = 'winniehell'

from lxml import etree

print('Loading schema...')
schema = etree.XMLSchema(etree=etree.XML('''
    <xsd:schema xmlns:xsd="http://www.w3.org/2001/XMLSchema">
        <xsd:import schemaLocation="schema/rif.xsd" namespace="http://automotive-his.de/schema/rif" />
        <xsd:import schemaLocation="schema/rif-xhtml.xsd" namespace="http://automotive-his.de/schema/rif-xhtml" />
    </xsd:schema>
'''))

for example_file in sorted(os.listdir('examples')):
    if not example_file.endswith('.xml'):
        continue

    print('Validating '+example_file+' ...')
    if not schema.validate(etree.parse('examples/'+example_file)):
        print(schema.error_log.last_error)
