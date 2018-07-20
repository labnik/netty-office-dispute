# netty-office-dispute

run with command:
java -server -XX:+AggressiveOpts -Xmx4G -Xms4G -jar target/hello-kitty.jar

wrk -t12 -c400 -d30s 'http://localhost:9090/?a=100&b=20'
Running 30s test @ http://localhost:9090/?a=100&b=20
  12 threads and 400 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     5.05ms    3.80ms 174.80ms   98.89%
    Req/Sec     4.36k     2.69k    7.76k    58.92%
  1427836 requests in 30.03s, 91.23MB read
  Socket errors: connect 157, read 214, write 0, timeout 0
Requests/sec:  47540.10
Transfer/sec:      3.04MB