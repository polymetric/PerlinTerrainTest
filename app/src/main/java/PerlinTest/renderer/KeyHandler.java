package PerlinTest.renderer;

import PerlinTest.CustomTerrainRenderer;
import org.lwjgl.glfw.GLFWKeyCallbackI;

import static org.lwjgl.glfw.GLFW.*;

public class KeyHandler implements GLFWKeyCallbackI {
    @Override
    public void invoke(long window, int key, int scancode, int action, int mods) {
        // pause
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) { CustomTerrainRenderer.paused = !CustomTerrainRenderer.paused; }

        if (key == GLFW_KEY_W) { CustomTerrainRenderer.isMovingFwd = action != GLFW_RELEASE; }
        if (key == GLFW_KEY_S) { CustomTerrainRenderer.isMovingBwd = action != GLFW_RELEASE; }
        if (key == GLFW_KEY_A) { CustomTerrainRenderer.isMovingLeft = action != GLFW_RELEASE; }
        if (key == GLFW_KEY_D) { CustomTerrainRenderer.isMovingRight = action != GLFW_RELEASE; }
        if (key == GLFW_KEY_SPACE) { CustomTerrainRenderer.isMovingUp = action != GLFW_RELEASE; }
        if (key == GLFW_KEY_LEFT_SHIFT) { CustomTerrainRenderer.isMovingDown = action != GLFW_RELEASE; }

        if (key == GLFW_KEY_R && action == GLFW_RELEASE ) { CustomTerrainRenderer.reloadChunks(); }
        if (key == GLFW_KEY_G && action == GLFW_RELEASE ) { CustomTerrainRenderer.resetPosition(); }
        if (key == GLFW_KEY_P && action == GLFW_RELEASE ) { CustomTerrainRenderer.printPosition(); }
    }
}
