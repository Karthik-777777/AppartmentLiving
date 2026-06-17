import json

log_path = "/Users/karthikraju/.gemini/antigravity/brain/886bd419-9e7c-4928-9c31-9e3ac9794feb/.system_generated/logs/transcript.jsonl"

with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        data = json.loads(line)
        step = data.get('step_index')
        if data.get('type') == 'RUN_COMMAND' and data.get('status') == 'DONE':
            content = data.get('content', '')
            if 'HTTP/' in content:
                print(f"STEP {step} RESPONSE:")
                print(content)
                print("="*60)
