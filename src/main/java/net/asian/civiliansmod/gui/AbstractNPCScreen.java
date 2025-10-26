package net.asian.civiliansmod.gui;

import net.asian.civiliansmod.CiviliansMod;
import net.asian.civiliansmod.entity.NPCEntity;
import net.asian.civiliansmod.networking.payload.npc.skin.ChangeBaseSkinPayload;
import net.asian.civiliansmod.networking.payload.npc.skin.ChangeSkinPayload;
import net.asian.civiliansmod.networking.NPCDataPayload;
import net.asian.civiliansmod.util.NPCUtil;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.Entity;
import net.asian.civiliansmod.custom_skins.SkinFolderManager;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractNPCScreen extends AbstractConfigScreen {
    private final NPCEntity npc;

    // Layout constants
    private static final int ENTITY_PREVIEW_SIZE = 25; // Downscaled preview
    private static final int ENTITY_SPACING = 58;     // Adjusted spacing
    private static final int COLUMN_WIDTH = 130;
    private final int defaultSkin;
    private int selectedVariant; // No variant is selected by default
    private int selectedVariantIndex = -1; // No variant is selected by default
    private int scrollOffset = 0;  // Current scroll offset
    private int maxScrollOffset;  // Maximum allowed scroll offset
    private boolean isScrolling = false; // True if currently dragging the scrollbar
    private int scrollbarHeight = 0;
    private int scrollbarY = 0;
    private final int originalVariant;
    private int scrollbarGrabOffset = 0;
    private TextFieldWidget nameInputField;
    private ButtonWidget upslimButton;
    private ButtonWidget updefaultButton;
    private float smoothHeadYaw = 0.0F;
    private float smoothPitch = 0.0F;

    /**
     * used to know if the variant should be saved.
     */
    boolean save = false;

    boolean follow;
    boolean stay;

    int startVariantIndex = 0;

    List<Integer> toRender = new ArrayList<>();


    public AbstractNPCScreen(NPCEntity npc) {
        this(npc, -1, NPCUtil.getSkins().indexOf(npc.getSkinManager().getIdSkin()));
    }

    public AbstractNPCScreen(NPCEntity npc, int selected, int defaultSkin) {
        super(npc, Text.literal("Change NPC Variant"));
        this.npc = npc;
        this.selectedVariant = selected;
        toRender = getSkinsToRender();
        this.originalVariant = NPCUtil.getSkins().indexOf(npc.getSkinManager().getIdSkin()); // Save the current variant to initialize the preview
        this.defaultSkin = defaultSkin;
        this.follow = npc.isFollowing();
        this.stay = npc.isPaused();
    }

    public AbstractNPCScreen(NPCEntity npc, int selected, int defaultSkin, int selectedVariantIndex, boolean follow, boolean stay) {
        super(npc, Text.literal("Change NPC Variant"));
        this.npc = npc;
        this.selectedVariant = selected;
        toRender = getSkinsToRender();
        this.selectedVariantIndex = selectedVariantIndex;
        this.originalVariant = NPCUtil.getSkins().indexOf(npc.getSkinManager().getIdSkin()); // Save the current variant to initialize the preview
        this.defaultSkin = defaultSkin;
        this.follow = follow;
        this.stay = stay;
    }

    protected abstract List<Integer> getSkinsToRender();


    @Override
    public boolean shouldPause() {
        return false;
    }

    private void updateScrollBarDimensions() {
        // Container dimensions
        int containerHeight = 166;
        int containerY = (this.height - containerHeight) / 2;

        // Total rows and visible rows calculation
        int totalRows = (int) Math.ceil(((double) (toRender.size()) / 3)); // Total number of rows
        int visibleRows = (containerHeight - 55) / ENTITY_SPACING; // Adjust relative to the container height


        this.maxScrollOffset = Math.max(0, (totalRows - visibleRows) * ENTITY_SPACING);

        startVariantIndex = scrollOffset / ENTITY_SPACING * 3;

        // Scroll bar total height based on the container
        int scrollBarTotalHeight = containerHeight - 55; // Leave padding inside the container


        this.scrollbarHeight = 15;
        this.scrollbarY = containerY + 40 + (int) ((float) this.scrollOffset / this.maxScrollOffset * (scrollBarTotalHeight - this.scrollbarHeight));
    }

    private void drawMainContainer(DrawContext context) {
        // Texture Identifier moved here
        Identifier guiTexture = Identifier.of("civiliansmod", "textures/gui/gui.png");

        // Define the container size (ensure it matches the dimensions of 'gui.png')
        int containerWidth = 256; // Width of 'gui.png'
        int containerHeight = 166; // Height of 'gui.png'

        // Calculate the position to center the container on the screen
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        // Draw the container texture (centered)
        context.drawTexture(
                RenderPipelines.GUI_TEXTURED,   // Specify the render layer function
                guiTexture,             // Texture Identifier
                containerX,             // X position
                containerY,             // Y position
                0,                      // U coordinate of the texture
                0,                      // V coordinate of the texture
                containerWidth,         // Width of the region to draw
                containerHeight,        // Height of the region to draw
                containerWidth,         // Width of the texture
                containerHeight         // Height of the texture
        );
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render the default elements
        super.render(context, mouseX, mouseY, delta);

        // Render the custom GUI container (Your GUI background)
        this.drawMainContainer(context);

        // Center text
        context.drawCenteredTextWithShadow(this.textRenderer, Text.literal("Civilian Customizer"), this.width / 2, 30, 0xFFFFFF);

        // Render center preview and variants
        renderCenterPreview(context, mouseX, mouseY);

        renderVariants(context, mouseX, mouseY, delta);

        // Render the scroll bar
        renderVanillaScrollBar(context);

        // Render the name input field
        this.nameInputField.render(context, mouseX, mouseY, delta);

        this.upslimButton.visible = this instanceof CustomNPCScreen;
        this.updefaultButton.visible = this instanceof CustomNPCScreen;

        for (var button : this.children()) {
            if (button instanceof ButtonWidget) {
                ((ButtonWidget) button).render(context, mouseX, mouseY, delta);
            }
        }
    }

    @Override
    protected void init() {
        super.init();

        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Wide"),
                button -> MinecraftClient.getInstance().setScreen(new DefaultNPCScreen(this.npc, this.selectedVariant, defaultSkin, selectedVariantIndex, follow, stay))
        ).dimensions(containerX + 82, containerY + 22, 39, 12).build());

        // Add Slim tab button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Slim"),
                button -> MinecraftClient.getInstance().setScreen(new SlimNPCScreen(this.npc, this.selectedVariant, defaultSkin, selectedVariantIndex, follow, stay))
        ).dimensions(containerX + 121, containerY + 22, 40, 12).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Custom"),
                button -> MinecraftClient.getInstance().setScreen(new CustomNPCScreen(this.npc, this.selectedVariant, defaultSkin, selectedVariantIndex, follow, stay))
        ).dimensions(containerX + 161, containerY + 22, 39, 12).build());

        this.addDrawableChild(ButtonWidget.builder(Text.literal("Save"), button -> {
            save = true;
            this.close();
            scrollOffset = 0;
            updateScrollBarDimensions();
        }).dimensions(containerX + 11, containerY + 136, 50, 14).build());

        this.upslimButton = ButtonWidget.builder(Text.literal("↑Slim"), button ->
                        SkinFolderManager.openFolder(SkinFolderManager.NPCModel.SLIM)) // Pass "slim", not "civiliansmod_skins_slim"
                .dimensions(containerX + 202, containerY + containerHeight - 37, 49, 20).build();
        this.addDrawableChild(upslimButton);

        this.updefaultButton = ButtonWidget.builder(Text.literal("↑Wide"), button ->
                        SkinFolderManager.openFolder(SkinFolderManager.NPCModel.WIDE)) // Pass "default", not "civiliansmod_skins_default"
                .dimensions(containerX + 202, containerY + containerHeight - 66, 49, 20).build();
        this.addDrawableChild(updefaultButton);

        String currentName = npc.getCustomName() != null ? npc.getCustomName().getString() : ""; // Use NPC's current name or empty string
        this.nameInputField = new TextFieldWidget(
                this.textRenderer,
                containerX + 5, containerY + 22, 62, 14, Text.literal("Enter NPC Name")
        );


        ButtonWidget pauseButton = ButtonWidget.builder(Text.literal(npc.isPaused() ? "Stay: On" : "Stay: Off"), button -> {
                    boolean newState = !npc.isPaused();
                    npc.setPaused(newState); // Update NPC's paused state
                    button.setMessage(Text.literal(newState ? "Stay: On" : "Stay: Off")); // Update button text
                }).dimensions(containerX + 202, containerY + containerHeight - 124, 49, 20) // Adjust position and size
                .build();
        this.addDrawableChild(pauseButton);

        ButtonWidget followButton = ButtonWidget.builder(Text.literal(npc.isFollowing() ? "Follow: On" : "Follow: Off"), button -> {
                    boolean newState = !npc.isFollowing();
                    npc.setFollowing(newState); // Update NPC's follow state
                    button.setMessage(Text.literal(newState ? "Follow: On" : "Follow: Off")); // Update button text
                }).dimensions(containerX + 202, containerY + containerHeight - 95, 49, 20) // Adjust position and size
                .build();

        this.addDrawableChild(followButton);

        this.nameInputField.setText(currentName); // Pre-fill the text field with the NPC's current name
        this.nameInputField.setMaxLength(32); // Limit to 32 characters
        this.addSelectableChild(this.nameInputField);

        int totalRows = 22; // Default + Slim = 21 rows for each panel
        int visibleRows = (this.height - 100) / ENTITY_SPACING; // Rows that fit on screen at once

        // maxScrollOffset is based on rows that are not visible
        this.maxScrollOffset = Math.max(0, (totalRows - visibleRows) * ENTITY_SPACING);

        // Update scroll bar dimensions
        updateScrollBarDimensions();
    }

    @Override
    public void close() {
        if (MinecraftClient.getInstance().player != null) {
            if (!save) {
                npc.getSkinManager().setIdSkin(NPCUtil.getNPCTexture(this.defaultSkin));
                npc.setFollowing(follow);
                npc.setPaused(stay);
                super.close();
                return;
            }

            NPCDataPayload payload = new NPCDataPayload(
                    npc.getUuid(),
                    nameInputField.getText(),
                    npc.isPaused(),
                    npc.isFollowing()
            );
            ClientPlayNetworking.send(payload); // Send data to the server

            if (npc.getSkinManager().getIdSkin().custom()) {
                ChangeSkinPayload payload1 = new ChangeSkinPayload(npc.getUuid(), npc.getSkinManager().getIdSkin().slim(), npc.getSkinManager().getIdSkin());
                ClientPlayNetworking.send(payload1); // Send data to the server
            } else {
                if (selectedVariantIndex == -1) {
                    super.close();
                    return;
                }
                ChangeBaseSkinPayload payload1 = new ChangeBaseSkinPayload(npc.getUuid(), selectedVariantIndex);
                ClientPlayNetworking.send(payload1);
            }
        }

        super.close();
    }

    private void renderVanillaScrollBar(DrawContext context) {
        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        int scrollBarX = containerX + 70; // Positioned near the right edge of the container
        int scrollBarY = containerY + 38; // Start 10 pixels below the top of the container
        int scrollBarHeight = containerHeight - 51; // Adjust for padding (20 pixels)

        context.fill(scrollBarX, scrollBarY, scrollBarX + 6, scrollBarY + scrollBarHeight, 0xFF202020);
        context.fill(scrollBarX + 1, this.scrollbarY, scrollBarX + 5, this.scrollbarY + this.scrollbarHeight, 0xFFAAAAAA);
    }


    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Container dimensions
        int containerWidth = 256;
        int containerX = (this.width - containerWidth) / 2;

        // Scroll bar position
        int scrollBarX = containerX + 70; // Match `renderVanillaScrollBar`
        // Match `renderVanillaScrollBar`

        // Check if clicking within the scroll handle
        if (mouseX >= scrollBarX && mouseX <= scrollBarX + 6 && mouseY >= this.scrollbarY && mouseY <= this.scrollbarY + this.scrollbarHeight) {
            this.isScrolling = true;

            // Capture the click offset within the scroll handle
            this.scrollbarGrabOffset = (int) (mouseY - this.scrollbarY);
            return true;
        }
        if (button == 0) { // Left mouse button
            int panelX = /*(3 * containerX / 4)*/  (containerX + 83);

            // Detect which variant is clicked based on the selected tab
            int clickedVariant = detectClickedVariant(mouseX, mouseY, panelX);

            if (clickedVariant != -1) {
                this.selectedVariant = clickedVariant;
                if (clickedVariant < toRender.size())
                    this.selectedVariantIndex = toRender.get(clickedVariant);
                this.npc.getSkinManager().setIdSkin(NPCUtil.getNPCTexture(selectedVariantIndex)); // Update NPC variant immediately

                if (!NPCUtil.getNPCTexture(clickedVariant).custom())
                    this.npc.getSkinManager().setBaseVariant(selectedVariantIndex);

                try (ErrorReporter.Logging logging = new ErrorReporter.Logging(npc.getErrorReporterContext(), CiviliansMod.LOGGER)) {
                    NbtWriteView nbtWriteView = NbtWriteView.create(logging, npc.getRegistryManager());
                    npc.writeData(nbtWriteView);
                } catch (Exception var11) {
                    CiviliansMod.LOGGER.warn("Failed to save player data for {}", npc.getName().getString());
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }


    private int detectClickedVariant(double mouseX, double mouseY, int panelX/*, boolean isDefaultTab*/) {
        int containerHeight = 166;
        int containerY = (this.height - containerHeight) / 2;
        int startY = containerY + 39; // Matches where variants start rendering

        // Column setup
        int columnWidth = (COLUMN_WIDTH / 3) - 5; // Adjusted for columns in renderVariants
        int columnOffset = 1;

        // Strict container boundaries
        if (mouseY < containerY || mouseY > containerY + containerHeight) {
            return -1; // Mouse click is entirely outside the vertical container area
        }

        int minIndex = Math.min(toRender.size() - this.startVariantIndex, 9);

        // Loop through all rendered variants
        for (int i = 0; i <= minIndex; i++) {
            // Current variant's row and column
            int rowIndex = (i) / 3; // Determine row
            int columnIndex = (i) % 3; // Determine column


            // Variant's calculated position
            int xPosition = panelX + columnIndex * (columnWidth + columnOffset) /*+ xRightOffset*/;
            int yPosition = startY + rowIndex * ENTITY_SPACING /*- scrollOffset*/;

            // Extra check: Skip rows rendered above the visible container
            if (yPosition < containerY || yPosition + ENTITY_SPACING > containerY + containerHeight) {
                continue; // Skip variants not actually visible
            }

            // Check if the mouse position falls within the variant's hover box
            if (mouseX >= xPosition && mouseX <= xPosition + columnWidth &&
                    mouseY >= yPosition && mouseY <= yPosition + ENTITY_SPACING) {
                return this.startVariantIndex + i; // Return the clicked variant index
            }
        }

        return -1;
    }


    private void renderCenterPreview(DrawContext context, int mouseX, int mouseY) {
        // Determine which skin/variant to preview
        int variantToRender = (selectedVariantIndex == -1) ? originalVariant : selectedVariantIndex;

        NPCEntity previewNPC;
        if (originalVariant == -1) {
            previewNPC = createBaseCenterPreviewNPC();
        } else {
            previewNPC = createCenterPreviewNPC(variantToRender);
        }

        //Disable AI and Silent
        previewNPC.setAiDisabled(true);
        previewNPC.setSilent(true);

        // GUI size and position
        int guiWidth = 256;
        int guiHeight = 166;
        int guiX = (this.width - guiWidth) / 2;
        int guiY = (this.height - guiHeight) / 2;

        //center preview position
        int previewX = guiX + 36;
        int previewY = guiY + (guiHeight / 2) + 45;

        // Calculate head rotation to follow the mouse
        float deltaX = (float) (mouseX - previewX);
        float deltaY = (float) (mouseY - previewY);

        // Set head yaw (horizontal rotation) and pitch (vertical rotation) for more subtle movements
        float sensitivityFactor = 2.0F; // Higher value means more subtle movements
        float targetHeadYaw = (-(float) Math.atan2(deltaX, 50.0) * (180F / (float) Math.PI)) / sensitivityFactor;
        float targetPitch = ((float) Math.atan2(deltaY, 50.0) * (180F / (float) Math.PI)) / sensitivityFactor;

        // Clamp the pitch to prevent extreme angles (e.g., head flipping)
        targetHeadYaw = Math.max(-35.0F, Math.min(35.0F, targetHeadYaw));
        targetPitch = Math.max(-25.0F, Math.min(25.0F, targetPitch));

        //old pitch = Math.max(-30.0F, Math.min(30.0F, pitch)); // Limit pitch to -30 to +30 degrees

        float smoothing = 0.15F;
        final float DEADZONE = 0.8F;

        if (Math.abs(targetHeadYaw - smoothHeadYaw) < 0.4F) targetHeadYaw = smoothHeadYaw;
        if (Math.abs(targetPitch   - smoothPitch)   < 0.4F) targetPitch   = smoothPitch;

        if (Math.abs(targetHeadYaw - smoothHeadYaw) > DEADZONE)
            smoothHeadYaw += (targetHeadYaw - smoothHeadYaw) * 0.2F;

        if (Math.abs(targetPitch - smoothPitch) > DEADZONE)
            smoothPitch += (targetPitch - smoothPitch) * 0.2F;

        if (smoothHeadYaw > 180.0F) smoothHeadYaw -= 360.0F;
        if (smoothHeadYaw < -180.0F) smoothHeadYaw += 360.0F;

        float bodyYaw = smoothHeadYaw * 0.1F;
        previewNPC.setYaw(bodyYaw);
        previewNPC.bodyYaw = bodyYaw;

        // Adjust body yaw to move less than the head
        previewNPC.setHeadYaw(smoothHeadYaw);
        previewNPC.setPitch(smoothPitch);

        // Render the entity
        renderEntity(context, previewX, previewY, 35, previewNPC);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (this.isScrolling) {

            int containerHeight = 166;
            int containerY = (this.height - containerHeight) / 2;

            // Scroll bar position and height
            int scrollBarY = containerY + 40; // Match `renderVanillaScrollBar` and `updateScrollBarDimensions`
            int scrollBarHeight = containerHeight - 55;

            // Adjust relativeY to account for the grab offset
            float relativeY = (float) (mouseY - scrollBarY - this.scrollbarGrabOffset);
            float scrollPercent = relativeY / (scrollBarHeight - this.scrollbarHeight);

            // Calculate new scrollOffset and clamp
            this.scrollOffset = Math.max(0, Math.min((int) (scrollPercent * maxScrollOffset), maxScrollOffset));

            // Snap scroll offset to the nearest row
            this.scrollOffset = (this.scrollOffset / ENTITY_SPACING) * ENTITY_SPACING;

            updateScrollBarDimensions();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        this.scrollOffset = (short) Math.max(0, Math.min(this.scrollOffset - (int) (verticalAmount * ENTITY_SPACING), maxScrollOffset));
        updateScrollBarDimensions();
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        this.isScrolling = false;

        // Reset the grab offset after releasing the scroll bar
        this.scrollbarGrabOffset = 0;
        return super.mouseReleased(mouseX, mouseY, button);
    }


    private void renderVariants(DrawContext context, int mouseX, int mouseY, float ignoredDelta) {
        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        // Initial Y position relative to the container
        int startY = containerY + 61;
        int panelX = containerX + 77; // Position Default tab models within the container

        // Adjust spacing for columns for better alignment
        int columnWidth = (COLUMN_WIDTH / 3) - 10; // Reduced width to bring columns closer
        int columnOffset = 6; // Fine-tune additional space between columns

        int minIndex = Math.min(toRender.size() - this.startVariantIndex, 6);
        for (int i = startVariantIndex; i < this.startVariantIndex + minIndex; i++) {
            // Compute the row and column positions for each variant
            int rowIndex = (i - startVariantIndex) / 3; // Divide into groups of 3 per row
            int columnIndex = (i - startVariantIndex) % 3; // Determine which column the model is in
            int xPosition = panelX + columnIndex * (columnWidth + columnOffset); // Adjust horizontal position
            int yPosition = startY + rowIndex * ENTITY_SPACING; // Adjust vertical position
            // Render the model for the current variant
            renderVariantPreview(context, xPosition, yPosition, i, mouseX, mouseY);
        }
    }

    private void renderVariantPreview(DrawContext context, int x, int y, int variantIndex, int mouseX, int mouseY) {
        NPCEntity previewNPC = createPreviewNPC(variantIndex);

        // Container dimensions
        int containerWidth = 256;
        int containerHeight = 166;
        int containerX = (this.width - containerWidth) / 2;
        int containerY = (this.height - containerHeight) / 2;

        // Clip rendering to the container bounds
        int maxX = containerX + containerWidth;
        int maxY = containerY + containerHeight;

        // Adjust the hover box dimensions
        int adjustedX = x + 5; // Narrow the hover box by reducing 1 pixel from the left
        int adjustedY = y - 24; // Move the top of the box higher
        int entityWidth = 39;   // Set a fixed width (e.g., 50 pixels)
        int entityHeight = ENTITY_SPACING;  // Set a fixed height (e.g., 50 pixels)

        // Ensure the variant preview stays within the container bounds
        if (adjustedX + entityWidth > maxX || adjustedX < containerX)
            return; // Skip rendering if out of bounds horizontally
        if (adjustedY + entityHeight > maxY || adjustedY < containerY)
            return; // Skip rendering if out of bounds vertically

        // Render the entity preview
        renderEntity(context, x + ENTITY_PREVIEW_SIZE, y + (ENTITY_SPACING / 2), ENTITY_PREVIEW_SIZE, previewNPC);
        // Check if the mouse is hovering over this variant
        if (mouseX >= adjustedX && mouseX <= adjustedX + entityWidth
                && mouseY >= adjustedY && mouseY <= adjustedY + entityHeight) {
            // Draw a white rectangle outline around the entity preview by filling in each edge
            int outlineThickness = 2; // Thickness of the outline

            // Top border
            context.fill(adjustedX, adjustedY,
                    adjustedX + entityWidth, adjustedY + outlineThickness,
                    0xFFFFFFFF);
            // Bottom border
            context.fill(adjustedX, adjustedY + entityHeight - outlineThickness,
                    adjustedX + entityWidth, adjustedY + entityHeight,
                    0xFFFFFFFF);
            // Left border
            context.fill(adjustedX, adjustedY,
                    adjustedX + outlineThickness, adjustedY + entityHeight,
                    0xFFFFFFFF);
            // Right border
            context.fill(adjustedX + entityWidth - outlineThickness, adjustedY,
                    adjustedX + entityWidth, adjustedY + entityHeight,
                    0xFFFFFFFF);
        }
    }

    private NPCEntity createPreviewNPC(int variantIndex) {
        World world = MinecraftClient.getInstance().world;

        @SuppressWarnings("unchecked")// Create a new preview NPC
        NPCEntity previewNPC = new NPCEntity((EntityType<? extends PathAwareEntity>) npc.getType(), world);

        //we set the slim variant
        previewNPC.getSkinManager().setIdSkin(NPCUtil.getNPCTexture(toRender.get(variantIndex)));

        // These properties disable animations and sounds during preview
        previewNPC.setAiDisabled(true);
        previewNPC.setSilent(true);
        previewNPC.setHeadYaw(0.0F);

        return previewNPC;
    }

    private NPCEntity createCenterPreviewNPC(int skinId) {
        World world = MinecraftClient.getInstance().world;

        @SuppressWarnings("unchecked")// Create a new preview NPC
        NPCEntity previewNPC = new NPCEntity((EntityType<? extends PathAwareEntity>) npc.getType(), world);

        //we set the slim variant
        previewNPC.getSkinManager().setIdSkin(NPCUtil.getNPCTexture(skinId));

        // These properties disable animations and sounds during preview
        previewNPC.setAiDisabled(true);
        previewNPC.setSilent(true);
        previewNPC.setHeadYaw(0.0F);

        return previewNPC;
    }

    private NPCEntity createBaseCenterPreviewNPC() {
        World world = MinecraftClient.getInstance().world;

        @SuppressWarnings("unchecked")// Create a new preview NPC
        NPCEntity previewNPC = new NPCEntity((EntityType<? extends PathAwareEntity>) npc.getType(), world);

        //we set the slim variant
        previewNPC.getSkinManager().setIdSkin(npc.getSkinManager().getIdSkin());

        // These properties disable animations and sounds during preview
        previewNPC.setAiDisabled(true);
        previewNPC.setSilent(true);
        previewNPC.setHeadYaw(0.0F);

        return previewNPC;
    }

    @SuppressWarnings("unchecked")
    private void renderEntity(DrawContext context, int x, int y, int scale, Entity entity) {
        if (!(entity instanceof LivingEntity living)) return;

        MinecraftClient client = MinecraftClient.getInstance();
        EntityRenderDispatcher dispatcher = client.getEntityRenderDispatcher();

        EntityRenderer<LivingEntity, ? extends EntityRenderState> renderer =
                (EntityRenderer<LivingEntity, ? extends EntityRenderState>) dispatcher.getRenderer(living);

        boolean isPreview = (scale > 30); // Center-Preview higher (~35), varianten are lower (~25)
        renderCaptured(renderer, living, context, x, y, scale, client, isPreview);
    }


    private <S extends EntityRenderState> void renderCaptured(
        EntityRenderer<LivingEntity, S> renderer,
        LivingEntity living,
        DrawContext context,
        int x, int y, int scale,
        MinecraftClient client,
        boolean isPreview) {

        S renderState = renderer.createRenderState();
        renderer.updateRenderState(living, renderState, client.getRenderTickCounter().getTickProgress(false));

        //rotation fix
        Vector3f translation = new Vector3f(0f, 0f, 0f);
        Quaternionf rotation = new Quaternionf();

        //need to rotate 180° and Y + 15°
        if (isPreview) {
            rotation.rotateZ((float) Math.toRadians(180f))
                    .rotateY((float) Math.toRadians(195f))
                    .rotateX((float) Math.toRadians(-5f));
        } else {
            rotation.rotateZ((float) Math.toRadians(180f))
                    .rotateY((float) Math.toRadians(165f))
                    .rotateX((float) Math.toRadians(7f));
        }
        Quaternionf cameraAngle = new Quaternionf().rotateX((float) Math.toRadians(15f));

        context.addEntity(renderState, scale, translation, rotation, cameraAngle,
                x - scale, y - (int)(scale * 2.5f), x + scale, y + (int)(scale * 2.5f));
    }
}