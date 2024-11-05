import json
import re

# EXPERIMENTAL: NÃO EXECUTAR PORQUE NÂO FUNCIONA A 100%

def parse_imn(file_path):
    nodes = {}
    current_node = None
    current_ip = None

    with open(file_path, 'r') as file:
        for line in file:
            line = line.strip()

            # Início de uma nova definição de nó
            if line.startswith("node "):
                # Salva o nó anterior, se houver
                if current_node and current_ip:
                    nodes[current_node]['ip'] = current_ip
                # Identifica o nome do nó e inicia uma nova entrada
                current_node = re.search(r'node (\w+)', line).group(1)
                nodes[current_node] = {"name": current_node, "ip": None, "neighbours": []}
                current_ip = None

            # Detecta o IP principal da primeira interface
            elif line.startswith("ip address") and current_ip is None:
                current_ip = re.search(r'ip address ([\d.]+)/\d+', line).group(1)

            # Identifica vizinhos a partir de "interface-peer"
            elif line.startswith("interface-peer") and current_node:
                match = re.search(r'interface-peer \{eth\d+ (\w+)\}', line)
                if match:
                    neighbour = match.group(1)
                    if neighbour not in nodes[current_node]["neighbours"]:
                        nodes[current_node]["neighbours"].append(neighbour)

        # Salva o último nó processado
        if current_node and current_ip:
            nodes[current_node]['ip'] = current_ip

    # Substitui nomes de vizinhos por seus IPs
    for node, data in nodes.items():
        data['neighbours'] = [nodes[neigh]["ip"] for neigh in data['neighbours'] if nodes[neigh]["ip"]]

    # Converte para lista e retorna
    json_output = [v for v in nodes.values()]
    return json_output

def save_to_json(data, output_file):
    with open(output_file, 'w') as json_file:
        json.dump(data, json_file, indent=4)

# Caminho para o arquivo .imn e para o arquivo de saída JSON
imn_file_path = '../esr-streaming-service/topologia/topologia.imn'  # Substitua pelo caminho real do arquivo .imn
json_output_path = '../esr-streaming-service/config/bootstrapper.json'

# Processar e salvar
parsed_data = parse_imn(imn_file_path)
save_to_json(parsed_data, json_output_path)
