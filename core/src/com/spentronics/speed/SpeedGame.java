package com.spentronics.speed;

import java.io.IOException;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.ExtendViewport;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

public class SpeedGame extends ApplicationAdapter {
	private Server server;
	private Client client;
	private boolean isServer;
	private boolean isClient;
	private Color clearColor;
	private Stage stage;
	private float accumulator = 0;
	
	private static final float MAX_STEP_SIZE = 0.25f;
	private static final float STEP_SIZE = 1/60f;
	
	@Override
	public void create () {
		stage = new Stage(new ExtendViewport(640,480)); //better #s?
		isServer = false;
		isClient = false;
		clearColor = Color.BLACK;
		Car car = new Car();
		stage.addActor(car);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		float delta = Gdx.graphics.getDeltaTime();
		stepPhysics(delta);
		stage.draw();
		
		if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
			if(!isClient && !isServer) {
				isServer = true;
				startServer();
			}
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.Q)) {
			if(!isClient && !isServer) {
				isClient = true;
				connectToServer();
			}
		}
		
		
		if (Gdx.input.isKeyJustPressed(Input.Keys.Z)) {
			if(isClient) {
				sendColor(Color.BLUE);
			}
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.X)) {
			if(isClient) {
				sendColor(Color.BROWN);
			}
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.C)) {
			if(isClient) {
				sendColor(Color.CHARTREUSE);
			}
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.V)) {
			if(isClient) {
				sendColor(Color.CORAL);
			}
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.B)) {
			if(isClient) {
				sendColor(Color.FIREBRICK);
			}
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.N)) {
			if(isClient) {
				sendColor(Color.GOLD);
			}
		}
		else if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
			if(isClient) {
				sendColor(Color.OLIVE);
			}
		}
	}
	
	@Override
	public void dispose() {
		stage.dispose();
	}
	
	private void startServer() {
		server = new Server();
		server.start();
		try {
			server.bind(6552, 6553);
			clearColor = Color.RED;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			isServer = false;
			e.printStackTrace();
		}
		Kryo kryo = server.getKryo();
		registerClasses(kryo);
		server.addListener(new Listener() {
		       public void received (Connection connection, Object object) {
		          if (object instanceof ColorRequest) {
		             ColorRequest request = (ColorRequest) object;
		             clearColor = request.color;
		             ColorRequest newRequest = new ColorRequest();
		             newRequest.color = request.color;
		             connection.sendTCP(newRequest);
		          }
		       }
		    });
	}
	
	private void connectToServer() {
		client = new Client();
	    client.start();
	    try {
			client.connect(5000, "108.80.34.170", 6552, 6553);
			clearColor = Color.GREEN;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			isClient = false;
			e.printStackTrace();
		}
	    Kryo kryo = client.getKryo();
	    registerClasses(kryo);
	    client.addListener(new Listener() {
	        public void received (Connection connection, Object object) {
	           if (object instanceof ColorRequest) {
	             ColorRequest response = (ColorRequest)object;
	             clearColor = response.color;
	           }
	        }
	     });
	}
	
	private void sendColor(Color color) {
	    ColorRequest request = new ColorRequest();
	    request.color = color;
	    client.sendTCP(request);
	}
	
	private void registerClasses(Kryo kryo) {
		kryo.register(ColorRequest.class);
		kryo.register(Color.class);
	}
	
	private void stepPhysics(float delta) {
		float frameTime = Math.min(delta, MAX_STEP_SIZE);
		accumulator += frameTime;
		while (accumulator >= STEP_SIZE) {
			stage.act(frameTime);
			accumulator -= STEP_SIZE;
		}
	}
}
