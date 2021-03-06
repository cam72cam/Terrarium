package net.gegy1000.terrarium.server.world.pipeline;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import net.gegy1000.terrarium.Terrarium;
import net.gegy1000.terrarium.server.world.coordinate.Coordinate;
import net.gegy1000.terrarium.server.world.generator.customization.GenerationSettings;
import net.gegy1000.terrarium.server.world.pipeline.adapter.RegionAdapter;
import net.gegy1000.terrarium.server.world.pipeline.component.AttachedComponent;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponent;
import net.gegy1000.terrarium.server.world.pipeline.component.RegionComponentType;
import net.gegy1000.terrarium.server.world.pipeline.source.tile.TiledDataAccess;
import net.gegy1000.terrarium.server.world.region.RegionData;
import net.gegy1000.terrarium.server.world.region.RegionTilePos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TerrariumDataProvider {
    private final ImmutableMap<RegionComponentType<?>, AttachedComponent<?>> attachedComponents;
    private final ImmutableList<RegionAdapter> adapters;

    private TerrariumDataProvider(
            ImmutableMap<RegionComponentType<?>, AttachedComponent<?>> attachedComponents,
            ImmutableList<RegionAdapter> adapters
    ) {
        this.attachedComponents = attachedComponents;
        this.adapters = adapters;
    }

    public static Builder builder() {
        return new Builder();
    }

    public RegionData populateData(GenerationSettings settings, RegionTilePos pos, Coordinate regionSize, int width, int height) {
        Map<RegionComponentType<?>, RegionComponent<?>> populatedComponents = new HashMap<>();
        for (AttachedComponent<?> attachedComponent : this.attachedComponents.values()) {
            RegionComponent<?> component = attachedComponent.createAndPopulate(pos, width, height);
            populatedComponents.put(attachedComponent.getType(), component);
        }
        RegionData data = new RegionData(populatedComponents);
        this.applyAdapters(settings, data, pos, width, height);
        return data;
    }

    private void applyAdapters(GenerationSettings settings, RegionData data, RegionTilePos pos, int width, int height) {
        for (RegionAdapter adapter : this.adapters) {
            try {
                adapter.adapt(settings, data, pos.getMinBufferedX(), pos.getMinBufferedZ(), width, height);
            } catch (Exception e) {
                Terrarium.LOGGER.warn("Failed to run adapter {}", adapter.getClass().getName(), e);
            }
        }
    }

    public ImmutableSet<RegionComponentType<?>> getAttachedComponentTypes() {
        return this.attachedComponents.keySet();
    }

    public static class Builder {
        private final Map<RegionComponentType<?>, AttachedComponent<?>> attachedComponents = new HashMap<>();
        private final List<RegionAdapter> adapters = new ArrayList<>();

        private Builder() {
        }

        public <T extends TiledDataAccess> Builder withComponent(RegionComponentType<T> type, DataLayerProducer<T> producer) {
            this.attachedComponents.put(type, new AttachedComponent<>(type, producer));
            return this;
        }

        public Builder withAdapter(RegionAdapter adapter) {
            this.adapters.add(adapter);
            return this;
        }

        public TerrariumDataProvider build() {
            return new TerrariumDataProvider(ImmutableMap.copyOf(this.attachedComponents), ImmutableList.copyOf(this.adapters));
        }
    }
}
