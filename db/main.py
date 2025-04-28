import json
from datetime import datetime

def input_data(folder):
    names = input().strip().split(', ')
    ids = input().strip().split(', ')

    images = []
    for i in range(len(names)):
        images.append(f"./{folder}/images/{names[i].replace(' ', '-').lower()}.png")

    def process_attribute(X, sep=','):
        if X[-7:] == "for all":
            X = X.strip().split(' ')
            try: 
                return [int(X[-3])]*len(names)
            except:
                try: 
                    return [float(X[-3])]*len(names)
                except:
                    return [X[-3]]*len(names)

        else:
            try: 
                return [int(item) for item in X.strip().split(f'{sep} ')]
            except:
                try: 
                    return [float(item) for item in X.strip().split(f'{sep} ')]
                except:
                    return X.strip().split(f'{sep} ')

    price_gold = process_attribute(input())  
    weight = process_attribute(input())      
    stock = process_attribute(input())       
    magical = process_attribute(input())     
    craft = process_attribute(input())  
    protection = process_attribute(input())    
    heavy = process_attribute(input())   
    #damage = process_attribute(input())     
    #description = process_attribute(input(), sep=";") 
    #speed = process_attribute(input())       
    #gravity = process_attribute(input())     
    has_discount = process_attribute(input())
    standard_discount = process_attribute(input()) 
    special_discount = process_attribute(input())  
    category = process_attribute(input())         
    

    current_time = datetime.now()
    timestamp = current_time.strftime("%d-%m-%Y, %H:%M:%S")

    items = []
    for i in range(len(names)):
        item = {
            "name": names[i],
            "id": ids[i],
            "image": images[i],
            "price_gold": price_gold[i],
            "weight": weight[i],
            "stock": stock[i],
            "magical": magical[i],
            "craft": craft[i],
            "protection": protection[i],
            "heavy": heavy[i],
            #"damage": damage[i],
            #"description": description[i],
            #"speed": speed[i],
            #"gravity": gravity[i],
            "has_discount": bool(has_discount[i]),
            "standard_discount": standard_discount[i],
            "special_discount": special_discount[i],
            "category": category[i],
            "created_at": timestamp,
            "updated_at": timestamp
        }
        items.append(item)

    return items


def save_to_json(data, folder):
    with open(f"./{folder}/setup.json", "w") as json_file:
        json.dump(data, json_file, indent=4)
    print("setup.json has been created.")


if __name__ == "__main__":
    folder = input()
    print(folder)
    data = input_data(folder)
    save_to_json(data, folder)
