using System;
using System.IO;
using System.Net;
using System.Net.Security;
using System.Net.Sockets;
using System.Threading;

namespace DesktopClientCli
{
    class Program
    {
        static void Main(string[] args)
        {
            TcpListener tcpListener = null;

            try
            {
                string localIP="Test";
                using (Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, 0))
                {
                    socket.Connect("192.168.1.172", 5000);
                    IPEndPoint endPoint = socket.LocalEndPoint as IPEndPoint;
                    localIP = endPoint.Address.ToString();
                }
                if (localIP.Equals("Test"))
                {
                    Console.WriteLine("Ip-Adress could not be retrieved");
                    Console.ReadLine();
                    return;
                }
                // tcpListener new TcpListener(Dos.GetHostAddresses("waser.htl-braunau.at")[0], 5000);
                tcpListener = new TcpListener(IPAddress.Parse(localIP), 5000);

                tcpListener.Start();
                Console.WriteLine("Server gestartet auf " + localIP + " mit Port 5000...");

                while(true)
                {
                    // Blocking Call, Warten, dass sich ein Client verbindet
                    TcpClient tcpClient = tcpListener.AcceptTcpClient(); 
                    
                    // Thread starten, in diesem wird die Kommunikation mit diesem Client abgewickelt
                    new Thread(new ParameterizedThreadStart(ClientThread)).Start(tcpClient);
                }
            }
            catch(Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                tcpListener?.Stop();
            }
        }

        static void ClientThread(object o)
        {
            TcpClient tcpClient = o as TcpClient;

            StreamWriter sw = null;
            StreamReader sr = null;

            try
            {
                NetworkStream ns = tcpClient?.GetStream();
                sr = new StreamReader(ns);

                Console.WriteLine("Request from: " + ((IPEndPoint)tcpClient.Client.RemoteEndPoint).Address.ToString());
                string line = sr.ReadLine();
                Console.WriteLine(line);
                line = sr.ReadLine();
                Console.WriteLine(line);
                line = sr.ReadLine();
                Console.WriteLine(line);
            }
            catch(Exception ex)
            {
                Console.WriteLine(ex.Message);
            }
            finally
            {
                sr?.Close();
                tcpClient?.Close();

                Console.WriteLine("Client getrennt");

                Console.WriteLine("\n\n\n\n");
            }
        }
    }
}