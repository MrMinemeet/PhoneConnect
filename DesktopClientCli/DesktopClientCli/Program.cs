using System;
using System.IO;
using System.Net;
using System.Net.Sockets;
using System.Security.Cryptography;
using System.Text;
using System.Threading;

namespace DesktopClientCli
{
    
    
    
    class Program
    {
        public const string SERVER_IP = "192.168.1.22";
        public const int SERVER_PORT = 5000;    
        public const int BROADCAST_PORT = 25001;
        public const string PUBLIC_KEY = "1234567890";
        private const string PRIVATE_KEY = "1234567890";


        public static string ServerID = "";
        
        
        
        // BROADCAST PATTERN:
        /*
         * {Server ID (SHA256)}
         * {PUBLIC KEY}
         * 
         * 
         */
        
        // NOTIFICATION PATTERN:
        /*
         * {Appname}
         * {Title}
         * {Message}
         * {Date/Time}
         */
    
    
        static void Main(string[] args)
        {
            ServerID = GetHashString(Guid.NewGuid().ToString());
            
            // Start Broadcast Thread
            Thread broadcastThread = new Thread(UpdateSenderBroadcastThread);
            broadcastThread.Start();
            
            
            
            TcpListener tcpListener = null;
            
            try
            {
                tcpListener = new TcpListener(IPAddress.Any, 25000);

                tcpListener.Start();
                Console.WriteLine("Server started! Port: " + SERVER_PORT);

                while(true)
                {
                    // Blocking Call, waiting until a client connects
                    TcpClient tcpClient = tcpListener.AcceptTcpClient(); 
                    
                    // Start thread that handles communication with client
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

        static void UpdateSenderBroadcastThread()
        {
            // Sends a Broadcast every 5 sec. so that Client can update IP
            
            
            UdpClient udpClient = new UdpClient();
            udpClient.Client.Bind(new IPEndPoint(IPAddress.Any, BROADCAST_PORT));

            while (true)
            {
                var data = Encoding.UTF8.GetBytes(ServerID + " " + PUBLIC_KEY);
                udpClient.Send(data, data.Length, IPAddress.Broadcast.ToString(), BROADCAST_PORT);
                Console.WriteLine("Broadcast sent");
                Thread.Sleep(5000);
            }
        }

        static void ClientThread(object o)
        {
            TcpClient tcpClient = o as TcpClient;
            StreamReader sr = null;

            try
            {
                NetworkStream ns = tcpClient?.GetStream();
                sr = new StreamReader(ns);
                
                // Receiving the notification data
                Console.WriteLine("Request from: " + ((IPEndPoint)tcpClient.Client.RemoteEndPoint).Address.ToString());
                // Receive Encrypted data String
                string line = sr.ReadLine();
                
                Console.WriteLine("Plain text: \n" + line);
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
        
        
        
        
        public static byte[] GetHash(string inputString)
        {
            HashAlgorithm algorithm = SHA256.Create();
            return algorithm.ComputeHash(Encoding.UTF8.GetBytes(inputString));
        }

        public static string GetHashString(string inputString)
        {
            StringBuilder sb = new StringBuilder();
            foreach (byte b in GetHash(inputString))
                sb.Append(b.ToString("X2"));

            return sb.ToString();
        }
    }
}