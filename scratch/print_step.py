import json

log_path = "/Users/karthikraju/.gemini/antigravity/brain/886bd419-9e7c-4928-9c31-9e3ac9794feb/.system_generated/logs/transcript.jsonl"

with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        data = json.loads(line)
        step = data.get('step_index')
        if step in (2027, 2028):
            print(f"STEP {step}:")
            print(json.dumps(data, indent=2))
            print("="*60)
