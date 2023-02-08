/*
Copyright 2014 Google Inc. All rights reserved.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/


package dan200.qcraft.shared;

import dan200.QCraft;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class QCraftCommand implements ICommand
{
    private String m_name;
    private List m_aliases;

    public QCraftCommand()
    {
        m_name = "qcraft";
        m_aliases = new ArrayList( 1 );
        m_aliases.add( m_name );
    }

    @Override
    public String getName()
    {
        return m_name;
    }


    @Override
    public String getUsage( ICommandSender icommandsender )
    {
        return "/" + m_name + " verify [playername]";
    }

    @Override
    public List getAliases()
    {
        return m_aliases;
    }

    @Override
    public void execute(MinecraftServer server, ICommandSender icommandsender, String[] astring )
    {
        if( !(icommandsender instanceof EntityPlayer) )
        {
            return;
        }

        EntityPlayer player = (EntityPlayer)icommandsender;
        String verb = (astring.length >= 1) ? astring[0] : null;
        if( verb != null && verb.equals( "verify" ) )
        {
            // verify
            EntityPlayer targetPlayer;
            if( astring.length >= 2 )
            {
                String targetPlayerName = astring[1];
                targetPlayer = server.getEntityWorld().getPlayerEntityByName( targetPlayerName );
            }
            else
            {
                targetPlayer = player;
            }

            if( targetPlayer == null )
            {
                sendChat( icommandsender, "There is no such player" );
            }
            else if( QCraft.canPlayerVerifyPortalServers( player ) )
            {
                QCraft.verifyUnverifiedLuggage( player, targetPlayer );
            }
            else if( QCraft.canAnybodyVerifyPortalServers() )
            {
                sendChat( icommandsender, "You must be an admin to verify this server link." );
            }
            else
            {
                sendChat( icommandsender, "This server does not allow incoming inter-server portals." );
            }
        }
        else
        {
            // Unknown
            sendUsage( icommandsender );
        }
    }

    @Override
    public boolean checkPermission(MinecraftServer server, ICommandSender sender) {
        return false;
    }

    @Override
    public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) {
        return null;
    }

    private void sendUsage( ICommandSender icommandsender )
    {
        sendChat( icommandsender, "Usage: " + getUsage( icommandsender ) );
    }

    private void sendChat( ICommandSender icommandsender, String text )
    {
        icommandsender.sendMessage( new TextComponentString( text ) );
    }



    @Override
    public boolean isUsernameIndex( String[] astring, int i )
    {
        return false;
    }

    @Override
    public int compareTo( ICommand iCommand )
    {

        return iCommand.getName().compareTo( getName() );

    }


}
