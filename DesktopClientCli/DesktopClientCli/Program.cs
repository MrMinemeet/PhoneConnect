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
        public const string SERVER_IP = "192.168.1.22";
        public const int SERVER_PORT = 5000;
    
    
        static void Main(string[] args)
        {
            TcpListener tcpListener = null;
            Console.WriteLine(Environment.MachineName);
            try
            {
                string localIP;
                using (Socket socket = new Socket(AddressFamily.InterNetwork, SocketType.Dgram, 0))
                {
                    socket.Connect(SERVER_IP, SERVER_PORT);
                    IPEndPoint endPoint = socket.LocalEndPoint as IPEndPoint;
                    localIP = endPoint.Address.ToString();
                }
                if (!localIP.Equals(SERVER_IP))
                {
                    Console.WriteLine("Ip-Address could not be retrieved");
                    Console.ReadLine();
                    return;
                }
                
                tcpListener = new TcpListener(IPAddress.Parse(localIP), 5000);

                tcpListener.Start();
                Console.WriteLine("Server started! IP: " + SERVER_IP + " Port: " + SERVER_PORT);

                while(true)
                {
                    // Blocking Call, waiting until a client connects
                    TcpClient tcpClient = tcpListener.AcceptTcpClient(); 
                    
                    // Start thread that handles communitcation with client
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
                
                // Receiving the notification data
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

                Console.WriteLine("Client disconnected");

                Console.WriteLine("\n\n\n\n");
            }
        }
    }
}