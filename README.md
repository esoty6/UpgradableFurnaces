# UpgradableFurnaces
Upgrade furnaces


Some of the logic and functionalities belongs to [@Jikoo](https://github.com/Jikoo)'s.

This is a variation of [EnchantableBlocks](https://github.com/Jikoo/EnchantableBlocks). If you want to be able to enchant furnaces instead of upgrading them by placing blocks, you should check out the original plugin.

This plugin allows you to upgrade: furnace, smoker and blast furnace with different blocks.

Available upgrades:
 - **EFFICIENCY** - smelt items faster, but uses more fuel
 - **FORTUNE** - get more result items from one source item
 - **FUEL EFFICIENCY** - fuel lasts longer

You can specify what block and how experience will it provide on placing on furnace. You can even make one type of block to upgrade different types of upgrades.


## Bonus calculation methods

There are two types of calculating modifier bonuses. One is by using sigmoid function and second one is by using steps. If you want to use steps you **MUST** specify the steps in the `config.yml` file in the `upgradeLevels` section. Otherwise you should stick to the sigmoid function which only needs `maxLevel` of each upgrade to be given and each modifier will be calculated via sigmoid function.

## Step method
You don't have to put every single level in the steps. If you set the levels of efficiency are: 1, 3, 5, plugin will automaticaly detect that max level of this upgrade is 5, and for level 2, it will use modifiers from level 1, and for level 4 it will use modifiers for level 3 etc.

