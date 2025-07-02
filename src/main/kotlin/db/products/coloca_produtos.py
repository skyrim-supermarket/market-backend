import json
import os
import requests

main_category = "Ammunition"

# Caminho do arquivo JSON
JSON_FILE = f'./{main_category.lower()}/{main_category.lower()}.json'
BASE_URL = "http://localhost:8080/newProduct/"

def get_endpoint(category):
    return f"{BASE_URL}{category.capitalize()}"

# Ler o JSON do arquivo
try:
    with open(JSON_FILE, 'r', encoding='utf-8') as file:
        data = json.load(file)
except Exception as e:
    print(f"[ERRO] Falha ao ler o JSON: {e}")
    exit()

# Enviar cada produto
for item in data:
    image_path = item.pop('image', None)  # Remove campo 'image' do dicionário

    category = item.get("category", "Unknown")
    endpoint = get_endpoint(main_category)

    # Preparar os dados do formulário
    form_data = {
        "productName": item["name"],
        "priceGold": item["price_gold"],
        "stock": item["stock"],
        "description": item["description"],
        "standardDiscount": item["standard_discount"],
        "specialDiscount": item["special_discount"],
        "magical": "true" if item["magical"] is not None else "false",
        "craft": item["craft"],
        "speed": item["speed"],
        "gravity": item["gravity"],
        "category": item["category"]
    }

    # Enviar com a imagem
    try:
        with open(image_path, 'rb') as image_file:
            files = {
                'image': (os.path.basename(image_path), image_file, 'image/png')
            }
            response = requests.post(endpoint, data=form_data, files=files)

        print(f"[{response.status_code}] Enviado para {endpoint}: {item['name']}")
    except FileNotFoundError:
        print(f"[ERRO] Imagem não encontrada: {image_path}")
    except Exception as e:
        print(f"[ERRO] Falha ao enviar {item['name']}: {e}")
