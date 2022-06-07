package net.dries007.tfc.client.model.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.model.AgeableListModel;
import net.minecraft.client.model.GoatModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

import net.dries007.tfc.common.entities.land.DairyAnimal;

public class TFCGoatModel extends QuadrupedModel<DairyAnimal>
{
    public static LayerDefinition createBodyLayer()
    {
        return GoatModel.createBodyLayer();
    }

    public TFCGoatModel(ModelPart root)
    {
        super(root, true, 19.0F, 1.0F, 2.5F, 2.0F, 24);
    }

    public void setupAnim(DairyAnimal goat, float limbSwing, float limbSwingAmount, float ageInTicks, float headYaw, float headPitch)
    {
        this.head.getChild("left_horn").visible = !goat.isBaby();
        this.head.getChild("right_horn").visible = !goat.isBaby();
        super.setupAnim(goat, limbSwing, limbSwingAmount, ageInTicks, headYaw, headPitch);
    }
}

