import cv2

input_video_path = "../esr-streaming-service/videos/output.Mjpeg"
output_video_path = "../esr-streaming-service/videos/val.Mjpeg"

cap = cv2.VideoCapture(input_video_path)

if not cap.isOpened():
    print("Erro ao abrir o vídeo.")
    exit()

with open(output_video_path, 'wb') as f_out:
    while True:
        ret, frame = cap.read()  # Lê o próximo frame

        if not ret:  # Sai do loop se não houver mais frames
            break

        ret, encoded_frame = cv2.imencode('.jpg', frame)
        if not ret:
            print("Erro ao codificar o frame.")
            continue

        frame_bytes = encoded_frame.tobytes()
        
        frame_size = f"{len(frame_bytes):05}"

        f_out.write(frame_size.encode("ascii"))
        f_out.write(frame_bytes)

cap.release()
print("Arquivo de saída criado com sucesso:", output_video_path)

