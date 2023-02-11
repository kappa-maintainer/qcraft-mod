package dan200.qcraft.render;

import dan200.qcraft.Reference;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ICustomModelLoader;
import net.minecraftforge.client.model.IModel;

public class QBlockBakedModelLoader implements ICustomModelLoader {
    public static final QBlockModel QMODEL = new QBlockModel();

    @Override
    public void onResourceManagerReload(IResourceManager iResourceManager) {

    }

    @Override
    public boolean accepts(ResourceLocation resourceLocation) {
        return resourceLocation.getNamespace().equals(Reference.MOD_ID) && resourceLocation.getPath().equals("qblock");

    }

    @Override
    public IModel loadModel(ResourceLocation resourceLocation) throws Exception {
        return QMODEL;
    }
}
