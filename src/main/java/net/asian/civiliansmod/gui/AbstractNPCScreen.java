// Add these imports at the top
import net.minecraft.client.gui.widget.SliderWidget;

// Add these fields in the AbstractNPCScreen class:
private SliderWidget wanderSlider;
private boolean battleBuddy;
private float wanderRadius;

// Update the constructor:
public AbstractNPCScreen(NPCEntity npc, int selected, int defaultSkin, int selectedVariantIndex, boolean follow, boolean stay) {
    super(npc, Text.literal("Change NPC Variant"));
    this.npc = npc;
    this.selectedVariant = selected;
    toRender = getSkinsToRender();
    this.selectedVariantIndex = selectedVariantIndex;
    this.originalVariant = NPCUtil.getSkins().indexOf(npc.getSkinManager().getIdSkin());
    this.defaultSkin = defaultSkin;
    this.follow = follow;
    this.stay = stay;
    this.battleBuddy = npc.isBattleBuddy();
    this.wanderRadius = npc.getWanderRadius();
}

// Update the init() method - add these buttons after the follow button:

@Override
protected void init() {
    super.init();
    int containerWidth = 256;
    int containerHeight = 166;
    int containerX = (this.width - containerWidth) / 2;
    int containerY = (this.height - containerHeight) / 2;

    // ... existing tab buttons (Wide, Slim, Custom) ...

    this.addDrawableChild(ButtonWidget.builder(Text.literal("Wide"),
            button -> MinecraftClient.getInstance().setScreen(new DefaultNPCScreen(this.npc, this.selectedVariant, defaultSkin, selectedVariantIndex, follow, stay))
    ).dimensions(containerX + 82, containerY + 22, 39, 12).build());

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
                    SkinFolderManager.openFolder(SkinFolderManager.NPCModel.SLIM))
            .dimensions(containerX + 202, containerY + containerHeight - 37, 49, 20).build();
    this.addDrawableChild(upslimButton);

    this.updefaultButton = ButtonWidget.builder(Text.literal("↑Wide"), button ->
                    SkinFolderManager.openFolder(SkinFolderManager.NPCModel.WIDE))
            .dimensions(containerX + 202, containerY + containerHeight - 66, 49, 20).build();
    this.addDrawableChild(updefaultButton);

    String currentName = npc.getCustomName() != null ? npc.getCustomName().getString() : "";
    this.nameInputField = new TextFieldWidget(
            this.textRenderer,
            containerX + 5, containerY + 22, 62, 14, Text.literal("Enter NPC Name")
    );

    // Stay button
    ButtonWidget pauseButton = ButtonWidget.builder(Text.literal(npc.isPaused() ? "Stay: On" : "Stay: Off"), button -> {
                boolean newState = !npc.isPaused();
                npc.setPaused(newState);
                button.setMessage(Text.literal(newState ? "Stay: On" : "Stay: Off"));
                
                // Update slider visibility
                if (wanderSlider != null) {
                    wanderSlider.visible = !newState && !npc.isFollowing();
                }
            }).dimensions(containerX + 202, containerY + containerHeight - 124, 49, 20)
            .build();
    this.addDrawableChild(pauseButton);

    // Follow button
    ButtonWidget followButton = ButtonWidget.builder(Text.literal(npc.isFollowing() ? "Follow: On" : "Follow: Off"), button -> {
                boolean newState = !npc.isFollowing();
                npc.setFollowing(newState);
                button.setMessage(Text.literal(newState ? "Follow: On" : "Follow: Off"));
                
                // Update slider visibility
                if (wanderSlider != null) {
                    wanderSlider.visible = !newState && !npc.isPaused();
                }
            }).dimensions(containerX + 202, containerY + containerHeight - 95, 49, 20)
            .build();
    this.addDrawableChild(followButton);

    // NEW: Battle Buddy button
    ButtonWidget battleBuddyButton = ButtonWidget.builder(
            Text.literal(npc.isBattleBuddy() ? "Battle: On" : "Battle: Off"), 
            button -> {
                boolean newState = !npc.isBattleBuddy();
                npc.setBattleBuddy(newState);
                if (newState && MinecraftClient.getInstance().player != null) {
                    npc.setOwner(MinecraftClient.getInstance().player);
                }
                button.setMessage(Text.literal(newState ? "Battle: On" : "Battle: Off"));
            }).dimensions(containerX + 148, containerY + containerHeight - 124, 49, 20)
            .build();
    this.addDrawableChild(battleBuddyButton);

    // NEW: Wander Scale slider (only visible when Stay and Follow are OFF)
    this.wanderSlider = new SliderWidget(
            containerX + 148, containerY + containerHeight - 95, 49, 20,
            Text.literal("Range: " + (int)this.wanderRadius),
            (this.wanderRadius - 1.0) / 63.0 // Normalize to 0-1 (range 1-64)
    ) {
        @Override
        protected void updateMessage() {
            wanderRadius = (float)(this.value * 63.0 + 1.0); // Convert back to 1-64
            this.setMessage(Text.literal("Range: " + (int)wanderRadius));
        }

        @Override
        protected void applyValue() {
            npc.setWanderRadius(wanderRadius);
        }
    };
    this.wanderSlider.visible = !npc.isPaused() && !npc.isFollowing();
    this.addDrawableChild(this.wanderSlider);

    this.nameInputField.setText(currentName);
    this.nameInputField.setMaxLength(32);
    this.addSelectableChild(this.nameInputField);

    int totalRows = 22;
    int visibleRows = (this.height - 100) / ENTITY_SPACING;
    this.maxScrollOffset = Math.max(0, (totalRows - visibleRows) * ENTITY_SPACING);
    updateScrollBarDimensions();
}

// Update the close() method to save battle buddy and wander radius:
@Override
public void close() {
    if (MinecraftClient.getInstance().player != null) {
        if (!save) {
            npc.getSkinManager().setIdSkin(NPCUtil.getNPCTexture(this.defaultSkin));
            npc.setFollowing(follow);
            npc.setPaused(stay);
            npc.setBattleBuddy(battleBuddy);
            npc.setWanderRadius(wanderRadius);
            super.close();
            return;
        }

        NPCDataPayload payload = new NPCDataPayload(
                npc.getUuid(),
                nameInputField.getText(),
                npc.isPaused(),
                npc.isFollowing(),
                npc.isBattleBuddy(),
                npc.getWanderRadius()
        );
        ClientPlayNetworking.send(payload);

        if (npc.getSkinManager().getIdSkin().custom()) {
            ChangeSkinPayload payload1 = new ChangeSkinPayload(npc.getUuid(), npc.getSkinManager().getIdSkin().slim(), npc.getSkinManager().getIdSkin());
            ClientPlayNetworking.send(payload1);
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