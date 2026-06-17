import json

log_path = "/Users/karthikraju/.gemini/antigravity/brain/886bd419-9e7c-4928-9c31-9e3ac9794feb/.system_generated/logs/transcript.jsonl"

target_steps = {2291, 2292, 2293, 2294, 2295, 2296, 2297, 2298}
with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        data = json.loads(line)
        step = data.get('step_index')
        if step in target_steps:
            print(f"--- STEP {step} ({data.get('type')}) ---")
            print(data.get('content'))
            print(data.get('tool_calls'))
            print()
