package com.abhay.stockSimGame;

import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.config.ConfigRetriever;
import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.rxjava.core.Vertx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;

// WAS TAKEN FROM A VERTX STARTER ON GITHUB AND MODIFIED FOR PERSONAL USE

public class ServerVerticle extends AbstractVerticle {

    // all variables located here
    private final Logger                                m_logger;
    private Router                                      m_router;
    private JsonObject                                  m_config = null;
    private HashMap<String, ClientConnection>           map = new HashMap<>();
    private Vertx                                       m_vertx;
    private ArrayList<Game>                             allGames = new ArrayList<>();

    public ServerVerticle(Vertx m_vertx) {
        super();
        m_logger = LoggerFactory.getLogger("APIVERTICLE");
        this.m_vertx = m_vertx;
    }

    @Override
    public void start() throws Exception {
        m_logger.info("Starting StarterVerticle");

        ConfigRetriever retriever = ConfigRetriever.create(vertx);
        retriever.getConfig(
                config -> {

                    m_logger.info("config retrieved");

                    if (config.failed()) {

                        m_logger.info("No config");

                    } else {

                        m_logger.info("Got config");
                        m_config = config.result();

                        // the actual creation of the server
                        HttpServer server = vertx.createHttpServer();
                        // whenever a new client has connected
                        server.websocketStream().handler(socket -> {
                            // creates a new ClientConnection for every new client that connects to the server
                            ClientConnection newClient = new ClientConnection(m_vertx, socket, m_logger, allGames);
                            // map will allow us to keep track of all of our ClientConnections using the respective
                            // socket IDs
                            map.put(socket.textHandlerID(), newClient);
                            m_logger.info("Connection ID=" + socket.textHandlerID());
                            // endHandler will run whenever a client has disconnected from the server
                            socket.endHandler((Void) -> {
                                // will need to remove the ClientConnection from the map (will also need to remove the
                                // players associated with this connection from the game)
                                map.remove(socket.textHandlerID());
                                m_logger.info("socket closed");
                            });
                        });
                        // kind of like a traffic controller in that any new clients will be directed to a different
                        // port after they have reached port 8080
                        server.listen(8080, res -> {
                            if (res.succeeded()) {
                                // this line is almost like an advertisement in a way, because it tells everyone
                                // that is now listening
                                m_logger.info("Server now listening on 8080");
                            } else {
                                m_logger.error("Server failed to start");
                            }
                        });
                    }
                }
        );
    }

    @Override
    public void stop() throws Exception {
        m_logger.info("Stopping StarterVerticle");
    }

}
