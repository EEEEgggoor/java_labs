package network;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;

public class NetworkClient {

    private final String host;
    private final int port;

    public NetworkClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    /* ---------- FILE ---------- */

    public void uploadFile(Path file, String target) throws Exception {
        try (Socket s = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(s.getOutputStream());
             DataInputStream in = new DataInputStream(s.getInputStream())) {

            out.writeUTF("UPLOAD_FILE");
            out.writeUTF(target);

            byte[] data = Files.readAllBytes(file);
            out.writeLong(data.length);
            out.write(data);

            in.readUTF(); // OK
        }
    }

    public void downloadFile(Path file, String target) throws Exception {
        try (Socket s = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(s.getOutputStream());
             DataInputStream in = new DataInputStream(s.getInputStream())) {

            out.writeUTF("DOWNLOAD_FILE");
            out.writeUTF(target);

            long size = in.readLong();
            byte[] data = in.readNBytes((int) size);
            Files.write(file, data);
        }
    }

    /* ---------- MEMORY ---------- */

    public void uploadObject(Object obj, String target) throws Exception {
        try (Socket s = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(s.getOutputStream())) {

            out.writeUTF("UPLOAD_MEM");
            out.writeUTF(target);

            ObjectOutputStream oos = new ObjectOutputStream(out);
            oos.writeObject(obj);
        }
    }

    public Object downloadObject(String target) throws Exception {
        try (Socket s = new Socket(host, port);
             DataOutputStream out = new DataOutputStream(s.getOutputStream());
             ObjectInputStream in =
                     new ObjectInputStream(s.getInputStream())) {

            out.writeUTF("DOWNLOAD_MEM");
            out.writeUTF(target);

            return in.readObject();
        }
    }
}
