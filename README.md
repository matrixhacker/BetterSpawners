# Better Spawners

## Purpose

This mod is intended to add to vanilla gameplay by making spawners more useful and fun.
<br/><br/>
Features:
<ul>
    <li>Mine spawners with any tool enchanted with silk touch.</li>
    <li>Clear a spawner's monster by using a specific item on it.</li>
    <li>Once cleared a spawner will begin spawning the next mob to die within a configured radius of it, if that mob is spawnable.</li>
    <li>The item used to clear a spawner is configurable.</li>
    <li>The mobs a spawner can be set to spawn are configurable.</li>
    <li>The silktouch enchanted tools that may be used to mine a spawner are configurable.</li>
    <li>The radius around a blank spawner in which a mob's death will cause the spawner to spawn it is configurable.</li>
</ul>

## Setup

This fabric mod is server side only and makes no changes client side. Install in the mods folder of your dedicated server or single player world.
A config file will be generated on the first run with descriptions of each configuration. It only requires the [Fabric API](https://www.curseforge.com/minecraft/mc-mods/fabric-api/).

Config: <b>betterspawners.json</b>
<br/>
The following configurations are available:
<br/>
<table>
    <tr>
        <th>Config</th>
        <th>Default Value</th>
        <th>Details</th>
    </tr>
    <tr>
        <td>enable_silktouch_spawner</td>
        <td>true</td>
        <td>Disables mining a spawner with silktouch if set to false.</td>
    </tr>
    <tr> 
        <td>enable_clear_spawner</td>
        <td>true</td>
        <td>Disables clearing a spawner if set to false.</td>
    </tr>
    <tr> 
        <td>clear_spawner_item</td>
        <td>end_crystal</td>
        <td>The item that can be used on a spawner to clear its spawn creature.</td>
    </tr>
    <tr>
        <td>enable_spawner_hostile_mobs</td>
        <td>true</td>
        <td>Disables spawners from being set to spawn hostile mobs if set to false.</td>
    </tr>
    <tr>
        <td>enable_spawner_passive_mobs</td>
        <td>false</td>
        <td>Disables spawners from being set to spawn passive mobs if set to false.</td>
    </tr>
    <tr>
        <td>enable_spawner_mob_list</td>
        <td>[ ]</td>
        <td>A comma separated list of mobs a spawner can be set to spawn. This can override disabled hostile and passive mobs.</td>
    </tr>
    <tr>
        <td>spawner_death_radius</td>
        <td>2</td>
        <td>
            The radius within a mob must die from a blank spawner for it to spawn that mob, if mob is configured to be spawnable.<br/>
            Min: 1<br/>
            Max: 48
        </td>
    </tr>
    <tr>
        <td>spawner_mining_tools</td>
        <td>[ Stone Pickaxe, Iron Pickaxe, Golden Pickaxe, Diamond Pickaxe, Netherite Pickaxe ]</td>
        <td>The silktouch tools that can be used to mine a spawner. This does not affect mining speed.</td>
    </tr>
</table>

## License

This mod is available under the CC0 license. Feel free to learn from it and incorporate it in your own projects.

## Remarks

Special thanks to LordDeatHunter who created SilkSpawners, which served as an inspiration for this mod.