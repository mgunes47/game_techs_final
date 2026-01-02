package com.blockworld.graphics;

import com.blockworld.world.Block;
import com.blockworld.world.Raycaster;
import com.blockworld.world.World;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3i;

import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

/**
 * 3D dünyayı render eden sınıf.
 */
public class Renderer {

    private Shader shader;
    private Shader wireframeShader;
    private Map<Block.Type, Mesh> blockMeshes;
    private Mesh wireframeCube;
    private Matrix4f modelMatrix;

    private Raycaster raycaster;
    private Raycaster.RaycastResult currentTarget;

    // Shader kaynak kodları (embedded)
    private static final String VERTEX_SHADER = """
            #version 330 core

            layout (location = 0) in vec3 aPos;
            layout (location = 1) in vec3 aColor;
            layout (location = 2) in vec3 aNormal;

            out vec3 fragColor;
            out vec3 fragNormal;
            out vec3 fragPos;

            uniform mat4 model;
            uniform mat4 view;
            uniform mat4 projection;

            void main() {
                fragPos = vec3(model * vec4(aPos, 1.0));
                fragColor = aColor;
                fragNormal = mat3(transpose(inverse(model))) * aNormal;

                gl_Position = projection * view * model * vec4(aPos, 1.0);
            }
            """;

    private static final String FRAGMENT_SHADER = """
            #version 330 core

            in vec3 fragColor;
            in vec3 fragNormal;
            in vec3 fragPos;

            out vec4 FragColor;

            uniform vec3 lightDir;
            uniform vec3 viewPos;

            void main() {
                // Ambient
                float ambientStrength = 0.4;
                vec3 ambient = ambientStrength * fragColor;

                // Diffuse
                vec3 norm = normalize(fragNormal);
                vec3 lightDirection = normalize(-lightDir);
                float diff = max(dot(norm, lightDirection), 0.0);
                vec3 diffuse = diff * fragColor * 0.8;

                // Specular
                float specularStrength = 0.1;
                vec3 viewDir = normalize(viewPos - fragPos);
                vec3 reflectDir = reflect(-lightDirection, norm);
                float spec = pow(max(dot(viewDir, reflectDir), 0.0), 16);
                vec3 specular = specularStrength * spec * vec3(1.0);

                vec3 result = ambient + diffuse + specular;
                FragColor = vec4(result, 1.0);
            }
            """;

    private static final String WIREFRAME_VERTEX_SHADER = """
            #version 330 core

            layout (location = 0) in vec3 aPos;

            uniform mat4 model;
            uniform mat4 view;
            uniform mat4 projection;

            void main() {
                gl_Position = projection * view * model * vec4(aPos, 1.0);
            }
            """;

    private static final String WIREFRAME_FRAGMENT_SHADER = """
            #version 330 core

            out vec4 FragColor;
            uniform vec3 lineColor;

            void main() {
                FragColor = vec4(lineColor, 1.0);
            }
            """;

    public Renderer() {
        this.shader = new Shader();
        this.wireframeShader = new Shader();
        this.blockMeshes = new HashMap<>();
        this.modelMatrix = new Matrix4f();
        this.raycaster = new Raycaster();
    }

    public void init() {
        try {
            // Ana shader'ı derle
            shader.initFromSource(VERTEX_SHADER, FRAGMENT_SHADER);
            shader.createUniform("model");
            shader.createUniform("view");
            shader.createUniform("projection");
            shader.createUniform("lightDir");
            shader.createUniform("viewPos");

            // Wireframe shader'ı derle
            wireframeShader.initFromSource(WIREFRAME_VERTEX_SHADER, WIREFRAME_FRAGMENT_SHADER);
            wireframeShader.createUniform("model");
            wireframeShader.createUniform("view");
            wireframeShader.createUniform("projection");
            wireframeShader.createUniform("lineColor");

            // Blok mesh'lerini oluştur
            createBlockMeshes();

            // Wireframe küp oluştur
            wireframeCube = createWireframeCube();

            System.out.println("Renderer başlatıldı!");

        } catch (Exception e) {
            System.err.println("Renderer başlatma hatası: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void createBlockMeshes() {
        // Her blok tipi için farklı renkli mesh
        blockMeshes.put(Block.Type.GRASS, Mesh.createCube(0.3f, 0.75f, 0.3f)); // Yeşil
        blockMeshes.put(Block.Type.DIRT, Mesh.createCube(0.55f, 0.35f, 0.2f)); // Kahverengi
        blockMeshes.put(Block.Type.STONE, Mesh.createCube(0.5f, 0.5f, 0.55f)); // Gri
        blockMeshes.put(Block.Type.WOOD, Mesh.createCube(0.6f, 0.4f, 0.25f)); // Açık kahve
        blockMeshes.put(Block.Type.SAND, Mesh.createCube(0.9f, 0.85f, 0.6f)); // Sarı
        blockMeshes.put(Block.Type.WATER, Mesh.createCube(0.2f, 0.5f, 0.9f)); // Mavi
    }

    private Mesh createWireframeCube() {
        // Küpün köşeleri (biraz büyük, bloğun dışına taşsın)
        float s = 0.505f;
        float[] positions = {
                -s, -s, -s, s, -s, -s, s, -s, -s, s, -s, s,
                s, -s, s, -s, -s, s, -s, -s, s, -s, -s, -s,
                -s, s, -s, s, s, -s, s, s, -s, s, s, s,
                s, s, s, -s, s, s, -s, s, s, -s, s, -s,
                -s, -s, -s, -s, s, -s, s, -s, -s, s, s, -s,
                s, -s, s, s, s, s, -s, -s, s, -s, s, s
        };
        float[] colors = new float[positions.length];
        float[] normals = new float[positions.length];
        int[] indices = new int[24];
        for (int i = 0; i < 24; i++)
            indices[i] = i;

        return new Mesh(positions, colors, normals, indices);
    }

    public void updateTarget(Camera camera, World world) {
        // Bakılan bloğu güncelle
        currentTarget = raycaster.cast(camera, world, 8.0f);
    }

    public void render(Camera camera, World world) {
        // Önce hedefi güncelle
        updateTarget(camera, world);

        shader.bind();

        // Matrisleri gönder
        shader.setUniform("view", camera.getViewMatrix());
        shader.setUniform("projection", camera.getProjectionMatrix());

        // Işık yönü (güneş gibi yukarıdan ve yandan)
        shader.setUniform("lightDir", new Vector3f(-0.3f, -1.0f, -0.5f));
        shader.setUniform("viewPos", camera.getPosition());

        // Tüm blokları render et
        for (Map.Entry<Vector3i, Block> entry : world.getBlocks().entrySet()) {
            Vector3i pos = entry.getKey();
            Block block = entry.getValue();

            // Model matrisini hesapla
            modelMatrix.identity();
            modelMatrix.translate(pos.x, pos.y, pos.z);
            shader.setUniform("model", modelMatrix);

            // Bloğu render et
            Mesh mesh = blockMeshes.get(block.getType());
            if (mesh != null) {
                mesh.render();
            }
        }

        // Seçim kutusunu (B tuşu ile açılan mod) çiz
        if (world.getSelectionBox().isSelecting() || world.getSelectionBox().hasSelection()) {
            renderSelectionBox(camera, world.getSelectionBox());
        }

        // Highlight çizimi (Baktığı bloğu göster - Siyah veya Mavi çerçeve)
        if (currentTarget != null) {
            Vector3f highlightColor = new Vector3f(0.0f, 0.0f, 0.0f); // Varsayılan Siyah
            if (world.getSelectionBox().isSelecting()) {
                highlightColor.set(0.0f, 0.0f, 1.0f); // Seçim modunda Mavi
            }
            renderBlockHighlight(camera, currentTarget.blockPos, highlightColor);
        }

        shader.unbind();
    }

    // SelectionBox çizimi (Kırmızı wireframe)
    private void renderSelectionBox(Camera camera, com.blockworld.world.SelectionBox box) {
        glDisable(GL_DEPTH_TEST);
        glLineWidth(4.0f);

        wireframeShader.bind();
        wireframeShader.setUniform("view", camera.getViewMatrix());
        wireframeShader.setUniform("projection", camera.getProjectionMatrix());

        // Kırmızı renk
        wireframeShader.setUniform("lineColor", new Vector3f(1.0f, 0.0f, 0.0f));

        Vector3i min = box.getMin();
        Vector3i max = box.getMax();

        // Eğer tek köşe seçiliyse o köşeyi göster
        if (min != null && max == null) {
            max = new Vector3i(min);
        }

        if (min != null) {
            float scaleX = (max.x - min.x) + 1.0f;
            float scaleY = (max.y - min.y) + 1.0f;
            float scaleZ = (max.z - min.z) + 1.0f;

            // WireframeCube merkezi 0,0,0'da ve boyutu 1,1,1 (yani -0.5 to 0.5)
            // Ancak bizim wireframeCube koordinatları -0.505f ile 0.505f arasında.
            // Bu sebeple scale ederken dikkatli olmalıyız veya pozisyonu ortalamalıyız.

            // Küp merkezi: min + scale/2
            float centerX = min.x + scaleX / 2.0f;
            float centerY = min.y + scaleY / 2.0f;
            float centerZ = min.z + scaleZ / 2.0f;

            modelMatrix.identity();
            modelMatrix.translate(centerX - 0.5f, centerY - 0.5f, centerZ - 0.5f);
            modelMatrix.scale(scaleX, scaleY, scaleZ);

            wireframeShader.setUniform("model", modelMatrix);

            glBindVertexArray(wireframeCube.getVaoId());
            glDrawElements(GL_LINES, wireframeCube.getVertexCount(), GL_UNSIGNED_INT, 0);
            glBindVertexArray(0);
        }

        wireframeShader.unbind();
        glLineWidth(1.0f);
        glEnable(GL_DEPTH_TEST);
    }

    private void renderBlockHighlight(Camera camera, Vector3i blockPos, Vector3f color) {
        glDisable(GL_DEPTH_TEST);
        glLineWidth(3.0f);

        wireframeShader.bind();

        wireframeShader.setUniform("view", camera.getViewMatrix());
        wireframeShader.setUniform("projection", camera.getProjectionMatrix());
        wireframeShader.setUniform("lineColor", color);

        modelMatrix.identity();
        modelMatrix.translate(blockPos.x, blockPos.y, blockPos.z);
        wireframeShader.setUniform("model", modelMatrix);

        // Lines olarak çiz
        glBindVertexArray(wireframeCube.getVaoId());
        glDrawElements(GL_LINES, wireframeCube.getVertexCount(), GL_UNSIGNED_INT, 0);
        glBindVertexArray(0);

        wireframeShader.unbind();

        glLineWidth(1.0f);
        glEnable(GL_DEPTH_TEST);
    }

    public Raycaster.RaycastResult getCurrentTarget() {
        return currentTarget;
    }

    public void renderBlockPreview(Block.Type type, float x, float y, float size, int screenWidth, int screenHeight) {
        // Eski viewport'u kaydetme şansımız yok (LWJGL wrapper yok), o yüzden assume
        // screenWidth/Height
        glViewport((int) x, (int) y, (int) size, (int) size);

        // Depth test'i temizle ki UI üstüne çizilsin
        glClear(GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);

        shader.bind();

        // Sabit kamera (İzometrik taklidi)
        Matrix4f view = new Matrix4f().lookAt(
                new Vector3f(2.0f, 2.0f, 2.0f), // Pozisyon
                new Vector3f(0.0f, 0.0f, 0.0f), // Hedef
                new Vector3f(0.0f, 1.0f, 0.0f) // Yukarı
        );

        // Perspektif
        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0f), 1.0f, 0.1f, 100.0f);

        shader.setUniform("view", view);
        shader.setUniform("projection", projection);
        shader.setUniform("lightDir", new Vector3f(-0.5f, -1.0f, 0.5f));
        shader.setUniform("viewPos", new Vector3f(2.0f, 2.0f, 2.0f));

        modelMatrix.identity();
        // Bloğu merkeze al
        modelMatrix.translate(-0.5f, -0.5f, -0.5f);

        shader.setUniform("model", modelMatrix);

        Mesh mesh = blockMeshes.get(type);
        if (mesh != null) {
            mesh.render();
        }

        shader.unbind();

        // Viewport'u eski haline getir
        glViewport(0, 0, screenWidth, screenHeight);
    }

    public void renderStructurePreview(com.blockworld.world.Structure structure, float x, float y, float size,
            int screenWidth, int screenHeight) {
        glViewport((int) x, (int) y, (int) size, (int) size);
        glClear(GL_DEPTH_BUFFER_BIT);
        glEnable(GL_DEPTH_TEST);

        shader.bind();

        // Yapının boyutlarını al ve kamerayı ona göre geri çek
        Vector3i dims = structure.getSize();
        float maxSize = Math.max(dims.x, Math.max(dims.y, dims.z));
        float dist = maxSize * 1.5f + 2.0f;

        Matrix4f view = new Matrix4f().lookAt(
                new Vector3f(dist, dist, dist),
                new Vector3f(0.0f, 0.0f, 0.0f),
                new Vector3f(0.0f, 1.0f, 0.0f));

        Matrix4f projection = new Matrix4f().perspective((float) Math.toRadians(45.0f), 1.0f, 0.1f, 100.0f);

        shader.setUniform("view", view);
        shader.setUniform("projection", projection);
        shader.setUniform("lightDir", new Vector3f(-0.5f, -1.0f, 0.5f));
        shader.setUniform("viewPos", new Vector3f(dist, dist, dist));

        // Yapıyı merkeze hizala
        float offsetX = -dims.x / 2.0f;
        float offsetY = -dims.y / 2.0f;
        float offsetZ = -dims.z / 2.0f;

        // Yapıdaki her bloğu çiz
        for (com.blockworld.world.Structure.StructureBlock sb : structure.getBlocks()) {
            modelMatrix.identity();
            modelMatrix.translate(offsetX + sb.offsetX, offsetY + sb.offsetY, offsetZ + sb.offsetZ);

            shader.setUniform("model", modelMatrix);

            Mesh mesh = blockMeshes.get(sb.type);
            if (mesh != null) {
                mesh.render();
            }
        }

        shader.unbind();
        glViewport(0, 0, screenWidth, screenHeight);
    }

    // cleanup metodu dosya sonunda olmalı, eklemeyi buraya yapıyoruz.

    public void cleanup() {
        shader.cleanup();
        wireframeShader.cleanup();
        for (Mesh mesh : blockMeshes.values()) {
            mesh.cleanup();
        }
        if (wireframeCube != null) {
            wireframeCube.cleanup();
        }
    }
}
