import json

log_path = "/Users/karthikraju/.gemini/antigravity/brain/886bd419-9e7c-4928-9c31-9e3ac9794feb/.system_generated/logs/transcript.jsonl"

routes = set()
with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        if '/api/' in line:
            # Look for api endpoints mentioned in the log
            # Let's print out lines containing api
            data = json.loads(line)
            content = str(data.get('content', '')) + str(data.get('tool_calls', ''))
            for word in content.split():
                if '/api/' in word:
                    routes.add(word)

print("Found routes:")
for r in sorted(routes):
    print(r)
