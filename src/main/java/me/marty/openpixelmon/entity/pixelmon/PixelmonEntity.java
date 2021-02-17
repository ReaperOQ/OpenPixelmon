package me.marty.openpixelmon.entity.pixelmon;

import me.marty.openpixelmon.OpenPixelmon;
import me.marty.openpixelmon.api.pixelmon.PokedexEntry;
import me.marty.openpixelmon.data.DataLoaders;
import net.minecraft.entity.EntityData;
import me.marty.openpixelmon.entity.CustomDataTrackers;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.ai.goal.LookAroundGoal;
import net.minecraft.entity.ai.goal.LookAtEntityGoal;
import net.minecraft.entity.ai.goal.WanderAroundGoal;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.world.LocalDifficulty;
import net.minecraft.world.ServerWorldAccess;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import java.util.Optional;
import java.util.UUID;

@SuppressWarnings("EntityConstructor")
public class PixelmonEntity extends AnimalEntity implements IAnimatable {

	protected static final TrackedData<Boolean> BOSS = DataTracker.registerData(PixelmonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	protected static final TrackedData<Optional<UUID>> OWNER_UUID = DataTracker.registerData(PixelmonEntity.class, TrackedDataHandlerRegistry.OPTIONAL_UUID);
	protected static final TrackedData<Integer> LEVEL = DataTracker.registerData(PixelmonEntity.class, TrackedDataHandlerRegistry.INTEGER);
	protected static final TrackedData<Identifier> PIXELMON_ID = DataTracker.registerData(PixelmonEntity.class, CustomDataTrackers.IDENTIFIER); //TODO: make an identifier tracked data handler registry
	protected static final TrackedData<Boolean> IS_MALE = DataTracker.registerData(PixelmonEntity.class, TrackedDataHandlerRegistry.BOOLEAN);
	private final AnimationFactory factory = new AnimationFactory(this);

	private int hp;

	public PixelmonEntity(EntityType<? extends AnimalEntity> entityType, World world) {
		super(entityType, world);
		this.hp = getMaxHp();
	}



	public void initialize(Identifier entry) {
		this.setPixelmonId(entry);
		hp = 0;
	}

	protected void initDataTracker() {
		super.initDataTracker();
		this.dataTracker.startTracking(BOSS, false);
		this.dataTracker.startTracking(PIXELMON_ID, OpenPixelmon.id("missing_no"));
		this.dataTracker.startTracking(OWNER_UUID, Optional.empty());
		this.dataTracker.startTracking(LEVEL, 0);
		this.dataTracker.startTracking(IS_MALE, true);
	}

	@Override
	public void writeCustomDataToTag(CompoundTag tag) {
		super.writeCustomDataToTag(tag);

		tag.putBoolean("boss", this.dataTracker.get(BOSS));
		tag.putString("pixelmonId", getPixelmonId().toString());
		if(this.dataTracker.get(OWNER_UUID).isPresent()){
			tag.putUuid("ownerUuid", this.dataTracker.get(OWNER_UUID).get());
		}
		tag.putInt("level", this.dataTracker.get(LEVEL));
	}

	@Override
	public void readCustomDataFromTag(CompoundTag tag) {
		super.readCustomDataFromTag(tag);
		if (tag.getKeys().contains("level")) {
			this.setBoss(tag.getBoolean("boss"));
			this.setPixelmonId(new Identifier(tag.getString("pixelmonId")));
			this.setLevel(tag.getInt("level"));
		}
		if(tag.getKeys().contains("ownerUuid")) {
			this.setOwnerUuid(tag.getUuid("ownerUuid"));
		}
	}

	private void setPixelmonId(Identifier pixelmonId) {
		this.dataTracker.set(PIXELMON_ID, pixelmonId);
	}

	private void setOwnerUuid(UUID uuid) {
		if(uuid != null){
			this.dataTracker.set(OWNER_UUID, Optional.of(uuid));
		}else {
			this.dataTracker.set(OWNER_UUID, Optional.empty());
		}
	}

	@Override
	public EntityData initialize(ServerWorldAccess world, LocalDifficulty difficulty, SpawnReason spawnReason, @Nullable EntityData entityData, @Nullable CompoundTag entityTag) {
		entityData = super.initialize(world, difficulty, spawnReason, entityData, entityTag);

		setBoss(getLevel() >= 20 && random.nextFloat() < 0.05F);
		setLevel(0);//pokedexEntry.minLevel + random.nextInt(pokedexEntry.evolutionLevel / 2));
		return entityData;
	}

	@Override
	protected void initGoals() {
		this.goalSelector.add(1, new LookAroundGoal(this));
		this.goalSelector.add(1, new WanderAroundGoal(this, 0.4d));
		this.goalSelector.add(3, new LookAtEntityGoal(this, PixelmonEntity.class, 10));
		this.goalSelector.add(3, new LookAtEntityGoal(this, PlayerEntity.class, 4));
	}

	public boolean isBoss() {
		return this.dataTracker.get(BOSS);
	}

	public void setBoss(boolean boss) {
		this.dataTracker.set(BOSS, boss);
	}

	public int getLevel() {
		return this.dataTracker.get(LEVEL);
	}

	public void setMale(boolean isMale) {
		this.dataTracker.set(IS_MALE, isMale);
	}

	public boolean isMale() {
		return this.dataTracker.get(IS_MALE);
	}

	public void setLevel(int level) {
		this.dataTracker.set(LEVEL, level);
	}

	public static DefaultAttributeContainer.Builder createPixelmonAttributes() { //TODO: finish
		return createMobAttributes();
	}

	@Nullable
	@Override
	public PassiveEntity createChild(ServerWorld world, PassiveEntity entity) {
		PixelmonEntity pixelmonEntity = new PixelmonEntity(getType(), world);
		pixelmonEntity.initialize(getPixelmonId());
		return pixelmonEntity;
	}

	@Override
	public EntityType<PixelmonEntity> getType() {
		return (EntityType<PixelmonEntity>) super.getType();
	}

	@Override
	public void registerControllers(AnimationData animationData) {
		//TODO:
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		if (source.getAttacker() instanceof PlayerEntity) {
			return false;
		}
		return super.damage(source, amount);
	}

	@Override
	public AnimationFactory getFactory() {
		return factory;
	}

	public Identifier getPixelmonId() {
		return this.dataTracker.get(PIXELMON_ID);
	}

	public PokedexEntry getPokedexEntry() {
		return DataLoaders.PIXELMON_MANAGER.getPixelmon().get(getPixelmonId());
	}

	public String getNickname() {
		return "nickname"; //TODO:
//		return OpenPixelmonTranslator.createTranslation(pokedexEntry.name).getString();
	}

	public boolean isWild() {
		return this.dataTracker.get(OWNER_UUID).isPresent();
	}

	public int getMaxHp() {
		return 100; //TODO: implement base stats, IV & EV properly,
	}

	public int getHp() {
		return hp;
	}

	public ServerPlayerEntity getOwner() {
		if(this.dataTracker.get(OWNER_UUID).isPresent()) {
			return world.getServer().getPlayerManager().getPlayer(this.dataTracker.get(OWNER_UUID).get()); //TODO
		}
		return null;
	}
}
