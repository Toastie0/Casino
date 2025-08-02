# Casino - Fabric Mod

A comprehensive Minecraft mod adding playing cards, poker chips, and casino-style gameplay to Minecraft 1.20.1 using Fabric.

**Author:** Toastie

## 🎯 **Current Status: Core Systems Complete and Production Ready**

This mod features **fully functional card, chip, economy, and multiplayer systems** that have been tested and confirmed working. Recent fixes have resolved all multiplayer issues, making this a complete item-based casino experience.

### ✅ **Production Ready Features**
- **52-Card Deck System**: Complete standard deck with proper multiplayer card rendering
- **Poker Chips**: 5 denominations with proper NBT handling and tooltips
- **Card Decks**: Draw cards, collect cards, multiple skin variants (4 designs)
- **Creative Tab**: Organized items with all variants properly displayed
- **Economy System**: ✅ **WORKING** - Impactor integration tested in multiplayer
- **GUI Shop**: ✅ **WORKING** - Chip/deck buying and selling fully functional
- **Commands**: ✅ **WORKING** - All `/chips` commands operational
- **Server Integration**: ✅ **WORKING** - Server-only Impactor installation confirmed
- **Multiplayer**: ✅ **FIXED** - Card rendering issues resolved with CustomModelData system
- **Client-Server Sync**: ✅ **WORKING** - All operations properly synchronized

### **Requirements**
- Minecraft 1.20.1
- Fabric Loader 0.16.14+
- Fabric API
- Java 17+

### **Dependencies**
- **Required**: Fabric API
- **Optional**: Impactor Economy (for server economy integration)
- **Included**: SGUI 1.2.2 (for GUI interfaces)

### **Download & Install**
1. Download the latest release from [Releases](../../releases)
2. Place the `.jar` file in your `mods` folder
3. Launch Minecraft with Fabric

## 🎮 **How to Use**

### **Playing Cards**
1. **Get a Deck**: Find card decks in the Playing Cards creative tab
2. **Draw Cards**: Right-click deck to draw individual cards
3. **Flip Cards**: Right-click any card to flip between face-up and face-down
4. **Collect Cards**: Shift+right-click deck to collect all matching cards from inventory

### **Poker Chips**
1. **Get Chips**: Available in creative tab or via `/chips buy` command
2. **Values**: White ($1), Red ($5), Green ($25), Blue ($50), Black ($100)
3. **Stacking**: Maximum 20 chips per stack

### **Commands** (Tested & Working)
- `/chips buy` - ✅ **WORKING** - Opens chip shop GUI
- `/chips sell` - ✅ **WORKING** - Sells all chips in inventory
- `/chips test` - ✅ **WORKING** - Shows current balance

## 🛠️ **Features in Detail**

### **Card System**
- **52 Cards**: Standard deck with Spades, Clubs, Diamonds, Hearts
- **Card Values**: Ace through King with proper naming
- **Card Skins**: 4 different card back designs
- **Smart Mechanics**: Decks only collect cards with matching designs
- **NBT Data**: Comprehensive data storage for card state

### **Economy Integration** (Tested & Working)
- **Impactor Support**: ✅ **CONFIRMED WORKING** - Server-side integration tested
- **Dummy Economy**: ✅ **WORKING** - Fallback economy for singleplayer testing
- **Server-Client**: ✅ **WORKING** - Economy operations handled server-side
- **Dynamic Detection**: ✅ **WORKING** - Automatically detects available economy systems

### **Technical Features**
- **Modern APIs**: Uses current Fabric 1.20.1 standards
- **NBT Handling**: Comprehensive data management
- **Client-Server**: Proper networking architecture
- **Error Handling**: Robust error checking and validation

## 🏗️ **Development**

### **Building from Source**
```bash
git clone https://github.com/YourUsername/Fabric_Playing_Cards.git
cd Fabric_Playing_Cards
./gradlew build
```

### **Project Structure**
```
src/main/java/com/ombremoon/playingcards/
├── PlayingCardsMod.java          # Main mod class
├── PCReference.java              # Constants and references
├── init/ModItems.java            # Item registration
├── item/                         # Item implementations
│   ├── ItemPokerChip.java       # Poker chip items
│   ├── ItemCard.java            # Face-up cards
│   ├── ItemCardCovered.java     # Face-down cards
│   └── ItemCardDeck.java        # Card decks
├── economy/                      # Economy system
│   ├── EconomyManager.java      # Main economy router
│   ├── ServerEconomyManager.java # Server-side economy
│   └── ClientEconomyManager.java # Client-side cache
├── gui/ChipShopGui.java         # GUI implementation
├── command/ChipsCommand.java    # Command system
└── util/                        # Utilities
    ├── CardHelper.java          # Card game logic
    └── ItemHelper.java          # NBT utilities
```

### **Contributing**
1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## 🎯 **Roadmap**

### **Phase 1: Core Systems Complete ✅**
- ✅ Economy integration with Impactor working
- ✅ GUI shop functionality confirmed
- ✅ All commands working in multiplayer
- ✅ Multiplayer card rendering fixed
- ✅ Client-server synchronization working

### **Phase 2: Blocks & Tables (High Priority)**
- [ ] Implement poker table blocks with multi-block structure
- [ ] Add bar stool seating with player mounting
- [x] **COMPLETED** - Casino carpet decorative blocks ✅
- [x] **COMPLETED** - Casino table blocks (dark oak premium design) ✅
- [ ] Add table interaction mechanics for card placement

### **Phase 3: Entities & 3D (Medium Priority)**
- [ ] 3D card entities for table placement with stacking
- [ ] 3D poker chip entities with realistic stacking physics
- [ ] Custom entity rendering for all 3D objects

### **Phase 4: Game Mechanics (Low Priority)**
- [ ] Basic poker game rules implementation
- [ ] Turn-based gameplay system
- [ ] Multi-player game sessions with state management
- [ ] AI opponents and tournament system
- [ ] Advanced card game mechanics

## 🔧 **Technical Details**

### **Card ID System**
Cards use a mathematical ID system (0-51):
```java
// Card ID calculation
cardId = (value - 1) * 4 + suit

// Examples:
// Ace of Spades: 0
// King of Hearts: 51
// 7 of Clubs: 25
```

### **NBT Data Structure**
```java
// Card NBT
{
    "CardID": 0-51,        // Which card
    "SkinID": 0-3,         // Card back design
    "Covered": true/false, // Face-up or face-down
    "CustomModelData": 0-3 // For texture variants
}

// Deck NBT
{
    "SkinID": 0-3,         // Deck design
    "CardsLeft": 0-52,     // Cards remaining
    "NextCardId": 0-51     // Next card to draw
}
```

### **Economy Architecture**
- **Server-Side**: All economy operations handled on server
- **Client-Side**: Balance caching for display
- **Reflection**: Impactor integration uses reflection (no compile dependency)
- **Fallback**: Dummy economy when Impactor unavailable

## 📄 **License**

This project is licensed under the GPL-3.0 License - see the [LICENSE](LICENSE) file for details.

## 🙏 **Credits**

### **Original Mod**
- **Authors**: OmbreMoon
- **Platform**: Minecraft Forge
- **Source**: Original Playing Cards mod

### **Fabric Port**
- **Developer**: Toastie
- **Platform**: Fabric 1.20.1
- **Achievements**: Complete item system, economy integration, multiplayer fixes
- **Status**: Core systems production-ready, advanced features in development

### **Dependencies**
- **Fabric API**: Required for mod functionality
- **SGUI**: Simple GUI library for interfaces
- **Impactor**: Optional economy integration

## 🔗 **Links**

- [Issues](../../issues) - Report bugs or request features
- [Wiki](../../wiki) - Detailed documentation
- [Fabric Documentation](https://fabricmc.net/wiki/) - Fabric modding guide
- [Original Mod](https://github.com/OmbreMoon/PlayingCards) - Original Forge version

---

**Note**: This mod is currently in **active development**. The card system is complete and functional, but economy features and advanced functionality are still being implemented. Contributions and testing are welcome!

### **Current Chip System**
- ✅ **9 Total Denominations**: $1, $5, $25, $50, $100, $500, $1000, $5000, $25000
- ✅ **Economy Integration**: All chips work with Impactor economy system
- ✅ **GUI Shop**: Buy/sell all chip denominations

---
