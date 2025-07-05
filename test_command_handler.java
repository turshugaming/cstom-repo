// 🧪 Test Command Handler - onCommand methoduna ekle

// onCommand methodunun switch case'ine ekle:
case "test":
    return handleTest(sender, args);

// Handler methodu:
private boolean handleTest(CommandSender sender, String[] args) {
    if (!(sender instanceof Player)) {
        sender.sendMessage(PREFIX + ChatColor.RED + "Bu komut sadece oyuncular tarafından kullanılabilir!");
        return true;
    }

    if (!sender.hasPermission("ultimateitems.admin")) {
        sender.sendMessage(PREFIX + ChatColor.RED + "Bunu yapmaya yetkiniz yok!");
        return true;
    }

    Player player = (Player) sender;

    if (args.length < 2) {
        player.sendMessage(PREFIX + ChatColor.RED + "Kullanım: /ui test <item_id>");
        player.sendMessage(ChatColor.GRAY + "Mevcut itemler:");
        for (String itemId : customItems.keySet()) {
            if (customItems.get(itemId).enabled) {
                player.sendMessage(ChatColor.YELLOW + "- " + itemId);
            }
        }
        return true;
    }

    String itemId = args[1];
    testItemGeneration(player, itemId);

    return true;
}

// sendHelp methoduna da ekle:
private void sendHelp(CommandSender sender) {
    sender.sendMessage(ChatColor.DARK_PURPLE + "========== " + ChatColor.LIGHT_PURPLE + "UltimateItems Komutları" + ChatColor.DARK_PURPLE + " ==========");
    sender.sendMessage(ChatColor.YELLOW + "/ui reload" + ChatColor.GRAY + " - Plugin'i yeniden yükle");
    sender.sendMessage(ChatColor.YELLOW + "/ui give <oyuncu> <item> [miktar]" + ChatColor.GRAY + " - Item ver");
    sender.sendMessage(ChatColor.YELLOW + "/ui list [kategori]" + ChatColor.GRAY + " - Itemleri listele");
    sender.sendMessage(ChatColor.YELLOW + "/ui info <item>" + ChatColor.GRAY + " - Item bilgilerini göster");
    sender.sendMessage(ChatColor.YELLOW + "/ui test <item>" + ChatColor.GRAY + " - 1.21.5 item testi yap"); // 🆕
    sender.sendMessage(ChatColor.YELLOW + "/ui create <item>" + ChatColor.GRAY + " - Yeni item oluştur");
    sender.sendMessage(ChatColor.YELLOW + "/ui pack reload" + ChatColor.GRAY + " - Resource pack'i yeniden oluştur");
    sender.sendMessage(ChatColor.YELLOW + "/ui debug" + ChatColor.GRAY + " - Debug modunu aç/kapat");
    sender.sendMessage(ChatColor.DARK_PURPLE + "==========================================");
}