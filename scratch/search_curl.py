import json

log_path = "/Users/karthikraju/.gemini/antigravity/brain/886bd419-9e7c-4928-9c31-9e3ac9794feb/.system_generated/logs/transcript.jsonl"

with open(log_path, 'r', encoding='utf-8') as f:
    for line in f:
        data = json.loads(line)
        if data.get('type') == 'RUN_COMMAND' or 'curl' in str(data.get('tool_calls', '')):
            cmd = ''
            for tc in data.get('tool_calls', []):
                if tc.get('name') == 'run_command':
                    cmd = tc['args'].get('CommandLine', '')
            if cmd and 'curl' in cmd:
                print(f"STEP {data.get('step_index')}: {cmd}")
                
        # Also print outputs of commands
        if data.get('type') == 'COMMAND_OUTPUT' or (data.get('type') == 'PLANNER_RESPONSE' and data.get('status') == 'DONE'):
            content = data.get('content', '')
            if 'HTTP/' in content or 'CF-RAY' in content or 'Server: cloudflare' in content:
                print(f"OUTPUT: {content[:400]}")
