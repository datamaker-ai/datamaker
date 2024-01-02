import json
from collections import defaultdict
groups = defaultdict(list)

c = open('components.json')
records = json.load(c)

md = open('sinks.md', 'w')
for record in records['payload']['sinks']:
    group = record['grouping']
    groups[group].append(record)

for group in sorted(groups.items()):

    md.write(f"## {group[0].upper()}\n")

    for record in group[1]:
        name = record['name']
        description = record['description']
        className = record['className']
        # "configProperties":[{"key":"amazon.sink.access.key.id","description":"AWS Credentials access key id","type":"STRING","defaultValue":"","possibleValues":[]}
        configs = record['configProperties']

        md.write(f"### {name}\n")
        md.write(f"Description: {description}\n\n")
        md.write(f"Class: {className}\n\n")

        md.write("Configuration: \n\n")
        for config in configs:
            md.write(f"- {config['description']}\n")
            md.write(f"\t- Type: {config['type']}\n")
            md.write(f"\t- Default value: {config['defaultValue']}\n")
            md.write(f"\t- Possible values: {', '.join(map(str, config['possibleValues']))}\n")
