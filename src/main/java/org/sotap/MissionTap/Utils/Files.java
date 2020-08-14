package org.sotap.MissionTap.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.sotap.MissionTap.MissionTap;

public final class Files {
    public static String cwd;
    public static FileConfiguration config;
    public static FileConfiguration dailyMissions;
    public static FileConfiguration weeklyMissions;
    public static FileConfiguration specialMissions;
    public static FileConfiguration meta;
    public static FileConfiguration translations;

    public static void init(MissionTap plugin) {
        cwd = plugin.getDataFolder().getPath();
        config = plugin.getConfig();
        dailyMissions = Files.load(".", "daily-missions.yml");
        weeklyMissions = Files.load(".", "weekly-missions.yml");
        specialMissions = Files.load(".", "special-missions.yml");
        meta = Files.load("./generated", "meta.yml");
        translations = Files.load(".", "translations.yml");
        if (isEmptyConfiguration(translations)) {
            LogUtil.warn("语言文件丢失，正在尝试从 GitHub 下载。");
            if (download("https://raw.githubusercontent.com/sotapmc/MaterialTranslation/master/translations.yml", new File(cwd), "translations.yml")) {
                LogUtil.success("下载成功。");
            } else {
                LogUtil.failed("下载失败，物品、方块、生物名称可能显示为 &enull&r。\n请手动前往 &bhttps://raw.githubusercontent.com/sotapmc/MaterialTranslation/master/translations.yml&r 下载文件并放置在 &bMissionTap&r 目录下。\n随后执行 &b/mt reload&r 即可。");
            }
        }
    }

    public static File getFile(File folder, String name) {
        File file = new File(folder, name);
        if (!folder.exists()) {
            folder.mkdir();
        }
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static FileConfiguration load(String path, String name) {
        return YamlConfiguration
                .loadConfiguration(getFile(new File(path.replace(path.length() == 1 ? "." : "./",
                        path.length() == 1 ? cwd : cwd + "/")), name));
    }

    public static boolean isEmptyConfiguration(ConfigurationSection config) {
        if (config == null)
            return true;
        return config.getKeys(false).size() == 0;
    }

    public static FileConfiguration loadPlayer(UUID u) {
        return load("./playerdata", u.toString() + ".yml");
    }

    public static FileConfiguration loadMissionFor(UUID u) {
        return load("./generated/player", u.toString() + ".yml");
    }

    public static void save(FileConfiguration data, String targetFile) {
        try {
            data.save(targetFile.replace("./", cwd + "/"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void savePlayer(FileConfiguration data, UUID u) {
        save(data, "./playerdata/" + u.toString() + ".yml");
    }

    public static void saveConfig() {
        save(config, "./config.yml");
    }

    public static void savePlayerMission(FileConfiguration data, UUID u) {
        save(data, "./generated/player/" + u.toString() + ".yml");
    }

    public static void saveMeta() {
        save(meta, "./generated/meta.yml");
    }

    /**
     * 获取指定类型的任务源文件
     * 
     * @param type 类型
     * @return 源文件的 FC 实例
     */
    public static FileConfiguration getMissions(String type) {
        switch (type) {
            case "daily":
                return dailyMissions;
            case "weekly":
                return weeklyMissions;
            default:
                return null;
        }
    }

    public static List<File> getSubfileList(File directory) {
        if (directory == null)
            return null;
        if (!directory.isDirectory())
            return null;
        List<File> files = new ArrayList<>();
        for (File f : directory.listFiles()) {
            if (f.isFile()) {
                files.add(f);
            }
        }
        return files;
    }

    /**
     * 获取有记录的所有玩家的 UUID 和其对应的 FC 实例所组成的 Map
     * 
     * @return Map(String,FC)
     */
    public static Map<UUID, FileConfiguration> getAllPlayerdata() {
        Map<UUID, FileConfiguration> result = new HashMap<>();
        try {
            for (File f : getSubfileList(new File(cwd + "/playerdata"))) {
                result.put(UUID.fromString(f.getName().replace(".yml", "")),
                        YamlConfiguration.loadConfiguration(f));
            }
            return result;
        } catch (NullPointerException e) {
            return null;
        }
    }

    /**
     * 获取有记录的所有玩家的 UUID
     * 
     * @return UUID 的 List
     */
    public static List<UUID> getAllPlayerUUID() {
        List<UUID> result = new ArrayList<>();
        try {
            for (File f : getSubfileList(new File(cwd + "/playerdata"))) {
                result.add(UUID.fromString(f.getName().replace(".yml", "")));
            }
            return result;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static FileConfiguration getPlayerMissions(UUID u) {
        return load("./generated/player", u.toString() + ".yml");
    }

    public static Map<String, FileConfiguration> getAllPlayerMissions() {
        Map<String, FileConfiguration> result = new HashMap<>();
        try {
            for (File f : getSubfileList(new File(cwd + "/generated/player"))) {
                result.put(f.getName().replace(".yml", ""), YamlConfiguration.loadConfiguration(f));
            }
            return result;
        } catch (NullPointerException e) {
            return null;
        }
    }

    public static boolean download(String url, File folder, String name) {
        try {
            URL httpURL = new URL(url);
            ReadableByteChannel rbc = Channels.newChannel(httpURL.openStream());
            FileOutputStream fos = new FileOutputStream(getFile(folder, name));
            fos.getChannel().transferFrom(rbc, 0, Integer.MAX_VALUE);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
