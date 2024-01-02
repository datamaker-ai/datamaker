import json

file = open('C:\\projects\\workspace\\datamaker\\service\\src\\main\\resources\\field-mappings.json', 'r')

file_sql = open('C:\\projects\\workspace\\datamaker\\service\\src\\main\\resources\\field-mappings.sql', 'w')

content = file.read()
records = json.loads(content)

template_base = """INSERT INTO FIELD_MAPPING (id, external_id, key, field_json) VALUES (null, random_uuid(), '%s', '{"name": "%s", "description": "", "languageTag": "%s", "className": "ca.breakpoints.datamaker.model.field.type.%sField" }');\n"""
template_config = """INSERT INTO FIELD_MAPPING (id, external_id, key, field_json) VALUES (null, random_uuid(), '%s', '{"name": "%s", "description": "", "languageTag": "%s", "className": "ca.breakpoints.datamaker.model.field.type.%sField", "config": { "%s": "%s" } }');\n"""
for record in records:

    json_data = json.loads(records[record])

    if "type" in json_data:
        file_sql.write(template_config % (record, record.split('-')[0].capitalize(), json_data['locale'], json_data['fieldType'].capitalize(), "field." + json_data['fieldType'] + ".type", json_data['type']))
    else:
        file_sql.write(template_base % (record, record.split('-')[0].capitalize(), json_data['locale'], json_data['fieldType'].capitalize()))

