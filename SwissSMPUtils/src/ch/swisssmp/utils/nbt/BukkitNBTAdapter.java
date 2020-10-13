package ch.swisssmp.utils.nbt;

import net.minecraft.server.v1_16_R2.*;
import net.querz.nbt.tag.*;
import net.querz.nbt.tag.Tag;
import org.bukkit.Bukkit;

import java.util.Map;

public class BukkitNBTAdapter {
    protected static CompoundTag adapt(NBTTagCompound nms) {
        if(nms==null) return null;
        CompoundTag result = new CompoundTag();
        for (String key : nms.getKeys()) {
            Tag<?> value = fromNMS(nms.get(key));
            if (value == null) continue;
            result.put(key, value);
        }

        return result;
    }

    protected static NBTTagCompound adapt(CompoundTag tag) {
        NBTTagCompound nms = new NBTTagCompound();
        for (Map.Entry<String, Tag<?>> entry : tag.entrySet()) {
            NBTBase nmsEntry = toNMS(entry.getValue());
            if (nmsEntry == null) continue;
            nms.set(entry.getKey(), nmsEntry);
        }
        return nms;
    }

    private static NBTBase toNMS(Tag<?> tag) {
        switch (tag.getID()) {
            case NBTConstants.TYPE_END:
                return null;
            case NBTConstants.TYPE_BYTE:
                return toNMS((ByteTag) tag);
            case NBTConstants.TYPE_SHORT:
                return toNMS((ShortTag) tag);
            case NBTConstants.TYPE_INT:
                return toNMS((IntTag) tag);
            case NBTConstants.TYPE_LONG:
                return toNMS((LongTag) tag);
            case NBTConstants.TYPE_FLOAT:
                return toNMS((FloatTag) tag);
            case NBTConstants.TYPE_DOUBLE:
                return toNMS((DoubleTag) tag);
            case NBTConstants.TYPE_BYTE_ARRAY:
                return toNMS((ByteArrayTag) tag);
            case NBTConstants.TYPE_STRING:
                return toNMS((StringTag) tag);
            case NBTConstants.TYPE_LIST:
                return toNMS((ListTag<?>) tag);
            case NBTConstants.TYPE_COMPOUND:
                return toNMS((CompoundTag) tag);
            case NBTConstants.TYPE_INT_ARRAY:
                return toNMS((IntArrayTag) tag);
            case NBTConstants.TYPE_LONG_ARRAY:
                return toNMS((LongArrayTag) tag);
            default:
                Bukkit.getLogger().warning("[BukkitNBTAdapter] Unknown NBT Tag Type " + tag.getID() + "!");
                return null;
        }
    }

    private static NBTBase toNMS(ByteTag tag) {
        return NBTTagByte.a(tag.asByte());
    }

    private static NBTBase toNMS(ShortTag tag) {
        return NBTTagShort.a(tag.asShort());
    }

    private static NBTBase toNMS(IntTag tag) {
        return NBTTagInt.a(tag.asInt());
    }

    private static NBTBase toNMS(LongTag tag) {
        return NBTTagLong.a(tag.asLong());
    }

    private static NBTBase toNMS(FloatTag tag) {
        return NBTTagFloat.a(tag.asFloat());
    }

    private static NBTBase toNMS(DoubleTag tag) {
        return NBTTagDouble.a(tag.asDouble());
    }

    private static NBTBase toNMS(ByteArrayTag tag) {
        return new NBTTagByteArray(tag.getValue());
    }

    private static NBTBase toNMS(StringTag tag) {
        return NBTTagString.a(tag.getValue());
    }

    private static NBTBase toNMS(ListTag<?> tag) {
        NBTTagList list = new NBTTagList();
        for (Tag<?> entry : tag) {
            NBTBase nms = toNMS(entry);
            if (nms == null) continue;
            list.add(nms);
        }
        return list;
    }

    private static NBTBase toNMS(CompoundTag tag) {
        return adapt(tag);
    }

    private static NBTBase toNMS(IntArrayTag tag) {
        return new NBTTagIntArray(tag.getValue());
    }

    private static NBTBase toNMS(LongArrayTag tag) {
        return new NBTTagLongArray(tag.getValue());
    }

    private static Tag<?> fromNMS(NBTBase nms) {
        switch (nms.getTypeId()) {
            case NBTConstants.TYPE_END:
                return null;
            case NBTConstants.TYPE_BYTE:
                return fromNMS((NBTTagByte) nms);
            case NBTConstants.TYPE_SHORT:
                return fromNMS((NBTTagShort) nms);
            case NBTConstants.TYPE_INT:
                return fromNMS((NBTTagInt) nms);
            case NBTConstants.TYPE_LONG:
                return fromNMS((NBTTagLong) nms);
            case NBTConstants.TYPE_FLOAT:
                return fromNMS((NBTTagFloat) nms);
            case NBTConstants.TYPE_DOUBLE:
                return fromNMS((NBTTagDouble) nms);
            case NBTConstants.TYPE_BYTE_ARRAY:
                return fromNMS((NBTTagByteArray) nms);
            case NBTConstants.TYPE_STRING:
                return fromNMS((NBTTagString) nms);
            case NBTConstants.TYPE_LIST:
                return fromNMS((NBTTagList) nms);
            case NBTConstants.TYPE_COMPOUND:
                return fromNMS((NBTTagCompound) nms);
            case NBTConstants.TYPE_INT_ARRAY:
                return fromNMS((NBTTagIntArray) nms);
            case NBTConstants.TYPE_LONG_ARRAY:
                return fromNMS((NBTTagLongArray) nms);
            default:
                Bukkit.getLogger().warning("[BukkitNBTAdapter] Unknown vanilla NBT Tag Type " + nms.getTypeId() + "!");
                return null;
        }
    }

    private static ByteTag fromNMS(NBTTagByte nms) {
        return new ByteTag(nms.asByte());
    }

    private static ShortTag fromNMS(NBTTagShort nms) {
        return new ShortTag(nms.asShort());
    }

    private static IntTag fromNMS(NBTTagInt nms) {
        return new IntTag(nms.asInt());
    }

    private static LongTag fromNMS(NBTTagLong nms) {
        return new LongTag(nms.asLong());
    }

    private static FloatTag fromNMS(NBTTagFloat nms) {
        return new FloatTag(nms.asFloat());
    }

    private static DoubleTag fromNMS(NBTTagDouble nms) {
        return new DoubleTag(nms.asDouble());
    }

    private static ByteArrayTag fromNMS(NBTTagByteArray nms) {
        return new ByteArrayTag(nms.getBytes());
    }

    private static StringTag fromNMS(NBTTagString nms) {
        return new StringTag(nms.asString());
    }

    private static ListTag<?> fromNMS(NBTTagList nms) {
        switch(nms.d_()){
            case NBTConstants.TYPE_END:
                return null;
            case NBTConstants.TYPE_BYTE:
                return fromNMSListByte(nms);
            case NBTConstants.TYPE_SHORT:
                return fromNMSListShort(nms);
            case NBTConstants.TYPE_INT:
                return fromNMSListInt(nms);
            case NBTConstants.TYPE_LONG:
                return fromNMSListLong(nms);
            case NBTConstants.TYPE_FLOAT:
                return fromNMSListFloat(nms);
            case NBTConstants.TYPE_DOUBLE:
                return fromNMSListDouble(nms);
            case NBTConstants.TYPE_BYTE_ARRAY:
                return fromNMSListByteArray(nms);
            case NBTConstants.TYPE_STRING:
                return fromNMSListString(nms);
            case NBTConstants.TYPE_LIST:
                return fromNMSListList(nms);
            case NBTConstants.TYPE_COMPOUND:
                return fromNMSListCompound(nms);
            case NBTConstants.TYPE_INT_ARRAY:
                return fromNMSListIntArray(nms);
            case NBTConstants.TYPE_LONG_ARRAY:
                return fromNMSListLongArray(nms);
            default:
                Bukkit.getLogger().warning("[BukkitNBTAdapter] Unknown vanilla NBT List Type " + nms.d_() + "!");
                return null;
        }
    }

    private static CompoundTag fromNMS(NBTTagCompound nms) {
        return adapt(nms);
    }

    private static IntArrayTag fromNMS(NBTTagIntArray nms) {
        return new IntArrayTag(nms.getInts());
    }

    private static LongArrayTag fromNMS(NBTTagLongArray nms) {
        return new LongArrayTag(nms.getLongs());
    }

    private static ListTag<ByteTag> fromNMSListByte(NBTTagList nms){
        ListTag<ByteTag> result = new ListTag<>(ByteTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagByte) entry));
        }
        return result;
    }

    private static ListTag<ShortTag> fromNMSListShort(NBTTagList nms){
        ListTag<ShortTag> result = new ListTag<>(ShortTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagShort) entry));
        }
        return result;
    }

    private static ListTag<IntTag> fromNMSListInt(NBTTagList nms){
        ListTag<IntTag> result = new ListTag<>(IntTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagInt) entry));
        }
        return result;
    }

    private static ListTag<LongTag> fromNMSListLong(NBTTagList nms){
        ListTag<LongTag> result = new ListTag<>(LongTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagLong) entry));
        }
        return result;
    }

    private static ListTag<FloatTag> fromNMSListFloat(NBTTagList nms){
        ListTag<FloatTag> result = new ListTag<>(FloatTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagFloat) entry));
        }
        return result;
    }

    private static ListTag<DoubleTag> fromNMSListDouble(NBTTagList nms){
        ListTag<DoubleTag> result = new ListTag<>(DoubleTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagDouble) entry));
        }
        return result;
    }

    private static ListTag<ByteArrayTag> fromNMSListByteArray(NBTTagList nms){
        ListTag<ByteArrayTag> result = new ListTag<>(ByteArrayTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagByteArray) entry));
        }
        return result;
    }

    private static ListTag<StringTag> fromNMSListString(NBTTagList nms){
        ListTag<StringTag> result = new ListTag<>(StringTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagString) entry));
        }
        return result;
    }

    private static ListTag<ListTag<?>> fromNMSListList(NBTTagList nms){
        ListTag<ListTag<?>> result = new ListTag<>(ListTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagList) entry));
        }
        return result;
    }

    private static ListTag<CompoundTag> fromNMSListCompound(NBTTagList nms){
        ListTag<CompoundTag> result = new ListTag<>(CompoundTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagCompound) entry));
        }
        return result;
    }

    private static ListTag<IntArrayTag> fromNMSListIntArray(NBTTagList nms){
        ListTag<IntArrayTag> result = new ListTag<>(IntArrayTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagIntArray) entry));
        }
        return result;
    }

    private static ListTag<LongArrayTag> fromNMSListLongArray(NBTTagList nms){
        ListTag<LongArrayTag> result = new ListTag<>(LongArrayTag.class);
        for(NBTBase entry : nms){
            result.add(fromNMS((NBTTagLongArray) entry));
        }
        return result;
    }
}
