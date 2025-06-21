package net.asian.civiliansmod.networking;

import net.asian.civiliansmod.CiviliansMod;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.Uuids;

import java.util.UUID;

public record PlayerLanguagePayload(UUID playerUUID, String language) implements CustomPayload {
    public static final CustomPayload.Id<PlayerLanguagePayload> ID = new CustomPayload.Id<>(Identifier.of(CiviliansMod.MOD_ID, "player_language"));

    public static final PacketCodec<RegistryByteBuf, PlayerLanguagePayload> CODEC = PacketCodec.tuple(
            Uuids.PACKET_CODEC, PlayerLanguagePayload::playerUUID,
            PacketCodecs.STRING, PlayerLanguagePayload::language,
            PlayerLanguagePayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

    public void handlePacket(ServerPlayNetworking.Context context) {
        CiviliansMod.playerLanguages.put(playerUUID, language);
    }
}
