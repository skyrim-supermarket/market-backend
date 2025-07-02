import json
import os
import requests
import re

def fetch_labels(category):
    url = f"http://localhost:8080/labels/{category.lower()}"
    resp = requests.get(url)
    resp.raise_for_status()
    print(resp.json())
    return resp.json()  # retorna lista de dicts { "name": ..., "type": ... }

def read_json(category):
    path = f"./{category.lower()}/{category.lower()}.json"
    with open(path, 'r', encoding='utf-8') as f:
        return json.load(f)

def get_endpoint(category):
    return f"http://localhost:8080/newProduct/{category.capitalize()}"

def camel_to_snake(name):
    # Transforma camelCase para snake_case (ex: priceGold → price_gold)
    s1 = re.sub('(.)([A-Z][a-z]+)', r'\1_\2', name)
    return re.sub('([a-z0-9])([A-Z])', r'\1_\2', s1).lower()

def build_form_data(item, labels):
    data = {}

    # Mapeamento manual de exceções
    special_key_map = {
        "productName": "name"
    }

    for label in labels:
        key = label["name"]
        if key == "id":
            continue

        t = label["type"]

        # Tratamento especial
        if key in special_key_map:
            val = item.get(special_key_map[key])
        else:
            # tentativa camelCase → snake_case
            snake_key = ''.join(['_' + c.lower() if c.isupper() else c for c in key]).lstrip('_')
            val = item.get(key) or item.get(snake_key)


        if val is None:
            if t == "boolean":
                data[key] = "false"
            else:
                data[key] = ""
            continue

        if t == "boolean":
            data[key] = "true" if val else "false"
        else:
            data[key] = str(val)

    return data


def main():
    main_category = input("Digite a categoria principal (ex: Armor, Arrow): ").strip()
    try:
        data = read_json(main_category)
    except Exception as e:
        print(f"[ERRO] não foi possível ler o JSON: {e}")
        return

    try:
        labels = fetch_labels(main_category)
    except Exception as e:
        print(f"[ERRO] falha ao obter labels: {e}")
        return

    endpoint = get_endpoint(main_category)

    for item in data:
        form_data = build_form_data(item, labels)
        image_path = item.get("image")

        files = {}
        if image_path and os.path.isfile(image_path):
            files["image"] = (
                os.path.basename(image_path),
                open(image_path, "rb"),
                "image/png"
            )
        else:
            print(f"[AVISO] Imagem não encontrada ou inválida: {image_path}")

        try:
            print("== Enviando para:", endpoint)
            print("== Dados do formulário:", form_data)
            print("== Arquivo da imagem:", files.get("image")[0] if files.get("image") else "Nenhuma imagem")

            resp = requests.post(endpoint, data=form_data, files=files)
            print(f"[{resp.status_code}] {item.get('name')} enviado para {endpoint} ({item})")
        except Exception as e:
            print(f"[ERRO] falha ao enviar {item.get('name')}: {e}")
        finally:
            if files.get("image"):
                files["image"][1].close()

if __name__ == "__main__":
    main()
