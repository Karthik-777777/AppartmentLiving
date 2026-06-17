import re

filepath = "/Users/karthikraju/StudioProjects/AppartmentLiving/app/src/main/java/com/simats/appartmentliving/ui/screens/AdminScreen.kt"

with open(filepath, 'r') as f:
    lines = f.readlines()

print("Composables in AdminScreen.kt:")
for idx, line in enumerate(lines):
    if '@Composable' in line:
        # Find next line declaring the function
        for offset in range(1, 5):
            next_line = lines[idx + offset].strip()
            if 'fun ' in next_line:
                print(f"Line {idx+1}: {next_line}")
                break
