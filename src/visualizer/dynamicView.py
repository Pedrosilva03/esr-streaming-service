import tkinter as tk
import socket
import threading

'''nodes = {
    '10.0.17.22': (120.0, 170.0),  # 10.0.17.22 -> 10.0.17.22
    '10.0.17.21': (72.0, 264.0),   # 10.0.17.21 -> 10.0.17.21
    '10.0.17.20': (72.0, 361.0),   # 10.0.17.20 -> 10.0.17.20
    '10.0.2.21': (72.0, 458.0),    # 10.0.2.21 -> 10.0.2.21
    '10.0.2.20': (122.0, 554.0),   # 10.0.2.20 -> 10.0.2.20
    '10.0.18.2': (407.0, 266.0),   # 10.0.18.2 -> 10.0.18.2
    '10.0.19.2': (408.0, 360.0),   # 10.0.19.2 -> 10.0.19.2
    '10.0.21.2': (410.0, 457.0),   # 10.0.21.2 -> 10.0.21.2
    '10.0.0.10': (937.0, 361.0),   # 10.0.0.10 -> 10.0.0.10
    '10.0.0.1': (841.0, 218.0),    # 10.0.0.1 -> 10.0.0.1
    '10.0.5.2': (742.0, 360.0),    # 10.0.5.2 -> 10.0.5.2
    '10.0.3.2': (599.0, 504.0),    # 10.0.3.2 -> 10.0.3.2
    '10.0.16.2': (552.0, 361.0)    # 10.0.16.2 -> 10.0.16.2
}

edges = [
    ('10.0.17.22', '10.0.18.2'), 
    ('10.0.17.21', '10.0.18.2'), 
    ('10.0.17.20', '10.0.18.2'), 
    ('10.0.17.22', '10.0.19.2'), 
    ('10.0.17.21', '10.0.19.2'), 
    ('10.0.17.20', '10.0.19.2'), 
    ('10.0.2.21', '10.0.19.2'), 
    ('10.0.2.20', '10.0.19.2'), 
    ('10.0.2.21', '10.0.21.2'), 
    ('10.0.2.20', '10.0.21.2'),
    ('10.0.18.2', '10.0.0.1'),
    ('10.0.18.2', '10.0.16.2'),
    ('10.0.19.2', '10.0.16.2'),
    ('10.0.21.2', '10.0.16.2'),
    ('10.0.21.2', '10.0.3.2'),
    ('10.0.16.2', '10.0.0.1'),
    ('10.0.16.2', '10.0.5.2'),
    ('10.0.16.2', '10.0.3.2'),
    ('10.0.3.2', '10.0.5.2'),
    ('10.0.3.2', '10.0.0.10'),
    ('10.0.5.2', '10.0.0.1'),
    ('10.0.5.2', '10.0.0.10'),
    ('10.0.0.1', '10.0.0.10')
]'''

nodes = {
    'Client1': (120.0, 170.0),
    'Client2': (72.0, 264.0),
    'Client3': (72.0, 361.0),
    'Client4': (72.0, 458.0),
    'Client5': (122.0, 554.0),
    'PoP1': (407.0, 266.0),
    'PoP2': (408.0, 360.0),
    'PoP3': (410.0, 457.0),
    'O1': (937.0, 361.0),
    'O2': (841.0, 218.0),
    'O3': (742.0, 360.0),
    'O4': (599.0, 504.0),
    'O5': (552.0, 361.0)
}

edges = [
    ('Client1', 'PoP1'), 
    ('Client2', 'PoP1'), 
    ('Client3', 'PoP1'), 
    ('Client1', 'PoP2'), 
    ('Client2', 'PoP2'), 
    ('Client3', 'PoP2'), 
    ('Client4', 'PoP2'), 
    ('Client5', 'PoP2'), 
    ('Client4', 'PoP3'), 
    ('Client5', 'PoP3'),
    ('PoP1', 'O2'),
    ('PoP1', 'O5'),
    ('PoP2', 'O5'),
    ('PoP3', 'O5'),
    ('PoP3', 'O4'),
    ('O5', 'O2'),
    ('O5', 'O3'),
    ('O5', 'O4'),
    ('O4', 'O3'),
    ('O4', 'O1'),
    ('O3', 'O2'),
    ('O3', 'O1'),
    ('O2', 'O1')
]

def draw_network(canvas):
    for node, (x, y) in nodes.items():
        canvas.create_oval(x-10, y-10, x+10, y+10, fill="blue")
        canvas.create_text(x, y+20, text=node)

    for start, end in edges:
        canvas.create_line(nodes[start], nodes[end], fill="black", width=2)

def update_path(canvas, node1, node2):
    canvas.create_line(nodes[node1], nodes[node2], fill="red", width=2)

def delete_path(canvas, node1, node2):
    canvas.create_line(nodes[node1], nodes[node2], fill="black", width=2)

def listener():
    serverSocket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    serverSocket.bind(('', 12345))
    serverSocket.listen()

    print("À escuta em " + socket.gethostname())

    while True:
        client_socket, _ = serverSocket.accept()
        data = client_socket.recv(1024).decode()[2:]

        nodess = data.split(" ")
        if int(nodess[2]) == 1:
            update_path(canvas, nodess[0], nodess[1])
        else:
            delete_path(canvas, nodess[0], nodess[1])

def start_listener():
    listener_thread = threading.Thread(target=listener, daemon=True)
    listener_thread.start()

root = tk.Tk()
canvas = tk.Canvas(root, width=1280, height=720)
canvas.pack()

draw_network(canvas)

start_listener()

root.mainloop()