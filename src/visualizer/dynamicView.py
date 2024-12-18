import tkinter as tk
import socket
import threading

nodes = {
    'Client1': (120.0, 170.0),
    'Client2': (72.0, 264.0),
    'Client3': (72.0, 361.0),
    'Client4': (72.0, 458.0),
    'Client5': (122.0, 554.0),
    'PoP1': (407.0, 266.0),
    'PoP2': (408.0, 360.0),
    'PoP3': (410.0, 457.0),
    'S1': (937.0, 361.0),
    'O2': (841.0, 218.0),
    'O3': (742.0, 360.0),
    'O4': (599.0, 504.0),
    'O5': (552.0, 361.0),
    'S2': (721.0, 578.0)
}

edges = {
    ('Client1', 'PoP1'): 0, 
    ('Client2', 'PoP1'): 0, 
    ('Client3', 'PoP1'): 0, 
    ('Client1', 'PoP2'): 0, 
    ('Client2', 'PoP2'): 0, 
    ('Client3', 'PoP2'): 0, 
    ('Client4', 'PoP2'): 0, 
    ('Client5', 'PoP2'): 0, 
    ('Client4', 'PoP3'): 0, 
    ('Client5', 'PoP3'): 0,
    ('PoP1', 'O2'): 0,
    ('PoP1', 'O3'): 0,
    ('PoP1', 'O5'): 0,
    ('PoP2', 'O5'): 0,
    ('PoP2', 'PoP3'): 0,
    ('PoP2', 'PoP1'): 0,
    ('PoP3', 'O5'): 0,
    ('PoP3', 'O4'): 0,
    ('O5', 'O2'): 0,
    ('O5', 'O3'): 0,
    ('O5', 'O4'): 0,
    ('O4', 'O3'): 0,
    ('O4', 'S1'): 0,
    ('O3', 'O2'): 0,
    ('O3', 'S1'): 0,
    ('O2', 'S1'): 0,
    ('O4', 'S2'): 0
}

streamCounters = {
    'Client1': (0, None),
    'Client2': (0, None),
    'Client3': (0, None),
    'Client4': (0, None),
    'Client5': (0, None),
    'PoP1': (0, None),
    'PoP2': (0, None),
    'PoP3': (0, None),
    'S1': (0, None),
    'O2': (0, None),
    'O3': (0, None),
    'O4': (0, None),
    'O5': (0, None),
    'S2': (0, None)
}

def drawStreamCounters():
    for node, (count, string) in streamCounters.items():
        if string is not None:
            canvas.delete(string)
        x, y = nodes[node]
        streamCounters[node] = (count, canvas.create_text(x, y-20, text=f'{count}'))

def draw_network(canvas):
    for node, (x, y) in nodes.items():
        canvas.create_oval(x-10, y-10, x+10, y+10, fill="blue")
        canvas.create_text(x, y+20, text=node)

    for (start, end), _ in edges.items():
        canvas.create_line(nodes[start], nodes[end], fill="black", width=2)

    drawStreamCounters()

def update_path(canvas, node1, node2):
    count = streamCounters[node2][0]
    count += 1
    streamCounters[node2] = (count, streamCounters[node2][1])
    edges[(node1, node2) if (node1, node2) in edges else (node2, node1)] += 1

    if edges[(node1, node2) if (node1, node2) in edges else (node2, node1)] == 1:
        canvas.create_line(nodes[node1], nodes[node2], fill="red", width=2)

    drawStreamCounters()

def delete_path(canvas, node1, node2):
    count = streamCounters[node2][0]

    count -= 1
    edges[(node1, node2) if (node1, node2) in edges else (node2, node1)] -= 1

    streamCounters[node2] = (count, streamCounters[node2][1])

    if edges[(node1, node2) if (node1, node2) in edges else (node2, node1)] == 0:
        canvas.create_line(nodes[node1], nodes[node2], fill="black", width=2)
    
    drawStreamCounters()

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
canvas = tk.Canvas(root, width=1080, height=720)
canvas.pack()

draw_network(canvas)

start_listener()

root.mainloop()