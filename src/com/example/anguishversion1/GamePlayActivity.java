package com.example.anguishversion1;

import java.util.ArrayList;

import org.andengine.engine.Engine;
import org.andengine.engine.camera.BoundCamera;
import org.andengine.engine.camera.ZoomCamera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
import org.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.entity.primitive.Line;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.util.FPSLogger;
//import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
import org.andengine.extension.tmx.TMXLayer;
import org.andengine.extension.tmx.TMXLayerObjectTiles;
import org.andengine.extension.tmx.TMXLoader.ITMXTilePropertiesListener;
import org.andengine.extension.tmx.TMXProperties;
import org.andengine.extension.tmx.TMXTile;
import org.andengine.extension.tmx.TMXTileProperty;
import org.andengine.extension.tmx.TMXTiledMap;
import org.andengine.input.touch.TouchEvent;
import org.andengine.input.touch.detector.PinchZoomDetector;
import org.andengine.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.andengine.input.touch.detector.ScrollDetector;
import org.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.andengine.input.touch.detector.SurfaceScrollDetector;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.BaseGameActivity;

import android.opengl.GLES20;
import android.util.DisplayMetrics;
/*
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
*/
public class GamePlayActivity extends BaseGameActivity implements
ITMXTilePropertiesListener,IOnSceneTouchListener, IScrollDetectorListener, IPinchZoomDetectorListener {
	
   ////////////////////////////////////////
   //     ENGINE AND TEXTURE FIELDS      //
   ////////////////////////////////////////
	
	public Engine mEngine;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private ITiledTextureRegion mcharacterTextureRegion;
	//private Body mPlayerBody;
	private int direction=3;
	private int newdirection=0;
	

    ////////////////////////////////////////
	
	///////////////////////////
	//     CAMERA STUFF      //
	///////////////////////////
	
		public int CAMERA_WIDTH = 720;
		public int CAMERA_HEIGHT = 480;
		public ZoomCamera mCamera;
		public ScrollDetector mScrollDetector;
		public PinchZoomDetector mPinchZoomDetector;
		public float mPinchZoomStartedCameraZoomFactor;
		private final float zoomDepth = 1.2f; //Smaller this is, the less we zoom in?
		private boolean mClicked = false;
		private float maxZoom = 0;
		
		public BoundCamera mBoundChaseCamera;
		
    ////////////////////////////////////////////////////
		
    ///////////////////////////
    // TMX  FILES AND MAP    //
    ///////////////////////////
		
		
		//private TMXTiledMap map;
		public TMXLayer currentLayer = null;
		public TMXTiledMap mMap = null;
		//Lines belong to tile hits, objects
		public ArrayList<Line> mDrawnLines = new ArrayList<Line>();
		public ArrayList<TMXLayerObjectTiles> mTileObject = new ArrayList<TMXLayerObjectTiles>();
		public boolean[][] TilesBlocked;
		org.andengine.util.color.Color selected = new org.andengine.util.color.Color(1f, 0f, 0f);
		org.andengine.util.color.Color unselected = new org.andengine.util.color.Color(0f, 0f, 0f);
		org.andengine.util.color.Color objectLines = new org.andengine.util.color.Color(0.4823f, 0.8313f, 0.3254f);
		
		private MapHandler mMapHandler;
		
		////////////////////////////////////////////////////
		//Controles
		private static final int DIALOG_ALLOWDIAGONAL_ID = 0;
		private BitmapTextureAtlas mOnScreenControlTexture;
		private ITextureRegion mOnScreenControlBaseTextureRegion;
		private ITextureRegion mOnScreenControlKnobTextureRegion;
        private DigitalOnScreenControl mDigitalOnScreenControl;
        private HUD mHUD;
        
        //////Scene//////
        
        private Scene mScene;
        //FixedStepPhysicsWorld mPhysicsWorld;
        
        
        /////////////// UI ///////////////
        
        //   Main button
        private BitmapTextureAtlas mfire_btn_Bitmap;
		private ITextureRegion mfire_btn_Texture;
        private Sprite fire_btn;
        


	@Override
	public EngineOptions onCreateEngineOptions() {
		// TODO Auto-generated method stub
		final DisplayMetrics displayMetrics = new DisplayMetrics();
		this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
		CAMERA_WIDTH = displayMetrics.widthPixels;
		CAMERA_HEIGHT = displayMetrics.heightPixels;
		this.mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		this.mBoundChaseCamera = new BoundCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		this.mCamera.setZoomFactor(this.zoomDepth);
		EngineOptions eOps = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
				new FillResolutionPolicy(), this.mCamera);
		eOps.getAudioOptions().setNeedsSound(true);
		return eOps;
	}
	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		
		this.mEngine = new Engine(pEngineOptions);
		return this.mEngine;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws Exception {
		// TODO Auto-generated method stub
		this.mMapHandler = new MapHandler(this);
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
		//Player
		
		this.mBitmapTextureAtlas = new BitmapTextureAtlas(this.getTextureManager(),2048, 2048, TextureOptions.BILINEAR);
		this.mcharacterTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mBitmapTextureAtlas , this, "StarwarSpriteSheet.PNG", 0, 0, 19, 8);
		this.mBitmapTextureAtlas.load();
        
		// Controls
		
		this.mOnScreenControlTexture = new BitmapTextureAtlas(this.getTextureManager(), 256, 128, TextureOptions.BILINEAR);
		this.mOnScreenControlBaseTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_base.png", 0, 0);
		this.mOnScreenControlKnobTextureRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
		this.mOnScreenControlTexture.load();
		
		// UI Buttons
		
		this.mfire_btn_Bitmap = new BitmapTextureAtlas(this.getTextureManager(), 256, 256, TextureOptions.BILINEAR);
		this.mfire_btn_Texture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(this.mfire_btn_Bitmap, this, "ui/Fire_button.png", 0, 0);
		this.mfire_btn_Bitmap.load();
		
		///////////////////////////////////
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mPinchZoomDetector = new PinchZoomDetector(this);
		 //Create the shape importer and open our shape definition file
       
		
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws Exception {
		// TODO Auto-generated method stub
		Scene mScene = new Scene();
		final FPSLogger fpsLogger = new FPSLogger();
		this.getEngine().registerUpdateHandler(fpsLogger);
		this.mHUD = new HUD();
		this.getEngine().getCamera().setHUD(this.mHUD);
		mScene.attachChild(mHUD);
		// Create physics world
		//this.mPhysicsWorld = new org.andengine.extension.physics.box2d.FixedStepPhysicsWorld(30, new Vector2(0, 0), false, 8, 1);
		
		pOnCreateSceneCallback.onCreateSceneFinished(mScene);
		
	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback) throws Exception {
		// TODO Auto-generated method stub
		pScene.setOnSceneTouchListener(this);
		pScene.setTouchAreaBindingOnActionMoveEnabled(true);
		//pScene.registerUpdateHandler(this.mPhysicsWorld);
		//pScene.registerUpdateHandler(mphy);
		
		
		//////// Loading Isomatric Map //////////
		
	
		this.mMapHandler.loadMap_Iso();
		
		/////////////////////////////////////////
		
		///////////  Add Player /////////////////
		
		final AnimatedSprite character = new AnimatedSprite(this.mCamera.getBoundsXMin()+100,this.mCamera.getBoundsYMax()/1.2f, this.mcharacterTextureRegion, this.getVertexBufferObjectManager());
		final PhysicsHandler physicsHandler = new PhysicsHandler(character);
		character.registerUpdateHandler(physicsHandler);
		character.setScale(1.3f);
        this.mCamera.setChaseEntity(character);
        //final FixtureDef playerFixtureDef = org.andengine.extension.physics.box2d.PhysicsFactory.createFixtureDef(0, 0, 0.5f);
        final Rectangle rect = new Rectangle(character.getX(),character.getHeight(),character.getWidth(), character.getHeight(), this.getVertexBufferObjectManager());
     	//mPlayerBody = org.andengine.extension.physics.box2d.PhysicsFactory.createBoxBody(this.mPhysicsWorld, rect, BodyType.DynamicBody,playerFixtureDef);
	/*
		this.mPhysicsWorld.registerPhysicsConnector(new org.andengine.extension.physics.box2d.PhysicsConnector(character, mPlayerBody, true, false){
			@Override
			public void onUpdate(float pSecondsElapsed){
				super.onUpdate(pSecondsElapsed);
				mCamera.updateChaseEntity();
			}
		});
		*/
	pScene.attachChild(character);
        pScene.registerUpdateHandler(new IUpdateHandler(){
            @Override
            public void reset() { }


            @Override
            public void onUpdate(final float pSecondsElapsed) {
            	
            	
            	
            	if (newdirection != direction) {
                    // down
                	if (direction == 8) character.animate(new long[]{40, 40, 40, 40, 40, 40, 40, 40,40, 40, 40, 40, 40, 40, 40, 40 , 40,  40}, 20, 37, true);
            	   // up 
            	     else if (direction == 2) character.animate(new long[]{40, 40, 40, 40, 40, 40, 40, 40,40, 40, 40, 40, 40, 40, 40, 40 , 40,  40}, 1, 18, true);
                   // right
                     else if (direction == 1) character.animate(new long[]{40, 40, 40, 40, 40, 40, 40, 40,40, 40, 40, 40, 40, 40, 40, 40 , 40,  40}, 39, 56, true);
                   // left
                     else if (direction == 7) character.animate(new long[]{40, 40, 40, 40, 40, 40, 40, 40,40, 40, 40, 40, 40, 40, 40, 40 , 40,  40}, 58, 75, true);
                   // up-left
                    else if (direction == 4) character.animate(new long[]{40, 40, 40, 40, 40, 40, 40, 40,40, 40, 40, 40, 40, 40, 40, 40 , 40,  40}, 96, 113, true);
                   // down-right
                     else if (direction == 5) character.animate(new long[]{40, 40, 40, 40, 40, 40, 40, 40,40, 40, 40, 40, 40, 40, 40, 40 , 40,  40}, 115, 132, true);
                   // down-left
                     else if (direction == 6) character.animate(new long[]{40, 40, 40, 40, 40, 40, 40, 40,40, 40, 40, 40, 40, 40, 40, 40 , 40,  40}, 134, 151, true);
                   // up-right
                     else if (direction == 3) character.animate(new long[]{40, 40, 40, 40, 40, 40, 40, 40,40, 40, 40, 40, 40, 40, 40, 40 , 40,  40}, 77, 94, true);
                	// no direction
                     else if (direction == 0) {
                    	 
                    	if(newdirection==8) {
      
                    	    character.stopAnimation(19);
                    	 
                    	}
                    	else if(newdirection==2){
                    		
                    		character.stopAnimation(0);
                    		
                    	}
                        else if (newdirection == 1) {
                        	
                        	character.stopAnimation(38);

						}
                        else if (newdirection == 7) {
                        	
                        	character.stopAnimation(57);

						}
                        else if (newdirection == 4) {
                        	
                        	character.stopAnimation(95);

						}
                        else if (newdirection == 5) {
                        	
                        	character.stopAnimation(114);

						}
                        else if (newdirection == 6) {
                             
                        	character.stopAnimation(133);
                        	
						}
                        else if (newdirection == 3) {
                        	
                        	character.stopAnimation(76);

						}
                     
                     
                     }
                	
                	newdirection = direction;
                	
                    }
                    
            }
            });
		this.mDigitalOnScreenControl = new DigitalOnScreenControl(0, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, this.getVertexBufferObjectManager(), new IOnScreenControlListener() {
			@Override
			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float pValueX, final float pValueY) {
				
				// mPlayerBody.setLinearVelocity(pValueX * 7, pValueY * 7);
				
	// track of directions
				
				if (pValueX == 0 && pValueY == 0) direction = 0;
	            // down
	            if (pValueX == 0 && pValueY > 0) direction = 8;
	            // up 
	            else if (pValueX == 0 && pValueY < 0) direction = 2;
	            // right
	            else if (pValueX > 0 && pValueY == 0 ) direction = 1;
	            // left
	            else if (pValueX < 0 && pValueY == 0) direction = 7;
	            // up-left
	            else if (pValueX < 0 && pValueY < 0) direction = 4;
	            // down-right
	            else if (pValueX > 0 && pValueY > 0) direction = 5;
	            // down-left
	            else if (pValueX < 0 && pValueY > 0) direction = 6;
	            // up-right
	            else if (pValueX > 0 && pValueY < 0) direction = 3;
			
			}
		});
		this.mDigitalOnScreenControl.getControlBase().setBlendFunction(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
		this.mDigitalOnScreenControl.getControlBase().setAlpha(2.5f);
		this.mDigitalOnScreenControl.getControlBase().setScaleCenter(0, 128);
		this.mDigitalOnScreenControl.getControlBase().setScale(1.4f);
		this.mDigitalOnScreenControl.getControlKnob().setScale(1.4f);
		this.mDigitalOnScreenControl.refreshControlKnobPosition();
        this.mDigitalOnScreenControl.setAllowDiagonal(true);
  
		pScene.setChildScene(this.mDigitalOnScreenControl);
		
		
		///////////////// Attach UI buttons ////////////////////////////
		
		fire_btn = new Sprite(CAMERA_WIDTH/1.16f, CAMERA_HEIGHT/1.32f, this.mfire_btn_Texture, this.getVertexBufferObjectManager());
		fire_btn.setScale(1.2f);
		pScene.getChildScene().attachChild(this.fire_btn);
		
		
		
		pOnPopulateSceneCallback.onPopulateSceneFinished();

		
	}

	@Override
	public void onPinchZoomStarted(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		//this.mPinchZoomStartedCameraZoomFactor = this.mCamera.getZoomFactor();
		this.mClicked = false;
	}

	@Override
	public void onPinchZoom(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		// TODO Auto-generated method stub
		//this.mCamera.setZoomFactor(Math.min(Math.max(this.maxZoom, this.mPinchZoomStartedCameraZoomFactor * pZoomFactor), this.zoomDepth));
		this.mClicked = false;
	}

	@Override
	public void onPinchZoomFinished(PinchZoomDetector pPinchZoomDetector,
			TouchEvent pTouchEvent, float pZoomFactor) {
		// TODO Auto-generated method stub
		this.mClicked = false;
	}

	@Override
	public void onScrollStarted(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onScroll(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		final float zoomFactor = mCamera.getZoomFactor();
		float xLocation = -pDistanceX / zoomFactor;
		float yLocation = -pDistanceY / zoomFactor;
		mCamera.offsetCenter(xLocation, yLocation);
		this.mClicked = false;
	}

	@Override
	public void onScrollFinished(ScrollDetector pScollDetector, int pPointerID,
			float pDistanceX, float pDistanceY) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		// TODO Auto-generated method stub
		if(this.mPinchZoomDetector != null) {
			this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);
			if(this.mPinchZoomDetector.isZooming()) {
				this.mScrollDetector.setEnabled(false);
			} else {
				if(pSceneTouchEvent.isActionDown()) {
					this.mScrollDetector.setEnabled(true);
				}
				this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
			}
		} else {
			this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		}

		if(pSceneTouchEvent.isActionUp()) {
			if(this.mClicked){
				//this.handleActionDown(pScene, pSceneTouchEvent);
			}
			this.mClicked = true;
		}
		return true;
	}
	@Override
	public void onTMXTileWithPropertiesCreated(TMXTiledMap pTMXTiledMap,
			TMXLayer pTMXLayer, TMXTile pTMXTile,
			TMXProperties<TMXTileProperty> pTMXTileProperties) {
		// TODO Auto-generated method stub
		
	}
	
	///////////////////////////////////////
	
	/**
	 * Setup the Camera if the map is isometric. 
	 * 
	 * @param height {@link Float} overall height of the map
	 * @param width {@link Float} overall width of the map
	 */
	public void setupCameraIsometric(final float height, final float width){
		//this.log.i(4, "Setup camera");
		if (this.CAMERA_WIDTH / height >= this.CAMERA_HEIGHT / width){
			this.maxZoom = this.CAMERA_WIDTH / height;
		}else {
			this.maxZoom = this.CAMERA_HEIGHT / width; 
		}
		/*
		 * We have to consider the map rows and columns do not match,
		 * so the xMin works out the bounds to the left of scene x=0,
		 * xMax to the right of scene x=0.
		 * The left hand side of the map is the rows, while the right is columns.
		 * We need to calculate the length of these for use in the bounds
		 * 
		 * We have to take into account the placement of the tile.
		 * The very left edge of the first tile is X = 0
		 * So when halving the width the right hand side is short changed by 
		 * half a tile width and the left gains, so add half a tile width to 
		 * the xMin and xMax to even this out.
		 */
		final float MAX_CAMERA_BOUND_ADDITION =60;
		final float halfTileWidth = this.mMap.getTileWidth() /2;
		final float xMin = this.mMap.getTileRows() * halfTileWidth;
		final float xMax = this.mMap.getTileColumns() * halfTileWidth;
		final float pBoundsXMin = halfTileWidth - xMin - MAX_CAMERA_BOUND_ADDITION;
		final float pBoundsYMin = -MAX_CAMERA_BOUND_ADDITION;
		final float pBoundsXMax = halfTileWidth + xMax + MAX_CAMERA_BOUND_ADDITION;
		final float pBoundsYMax = height + MAX_CAMERA_BOUND_ADDITION;
		this.mCamera.setBounds(pBoundsXMin, pBoundsYMin, pBoundsXMax, pBoundsYMax);
		this.mCamera.setBoundsEnabled(true);
		//this.mBoundChaseCamera.setZoomFactor(0);
		this.resetCamera();
	}
	
	public void resetCamera(){
		
		//this.mCamera.setZoomFactor(this.zoomDepth);
		this.mCamera.setCenter(0,0);
		
	}
	

	
}
