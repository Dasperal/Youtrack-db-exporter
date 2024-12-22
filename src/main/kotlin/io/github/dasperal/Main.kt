// Copyright © 2024 Leonid Murin (Dasperal)
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package io.github.dasperal

import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.env.Environments.newInstance

object Main
{
    private val issue_links: MutableList<IssueLink> = ArrayList()

    // Arg0 - path to youtrack db
    @JvmStatic
    fun main(args: Array<String>)
    {
        val env = newInstance(args[0])
        val store = PersistentEntityStores.newInstance(env, "teamsysstore")
        store.executeInTransaction {
            find_issue_links(it.getAll("IssueLinkPrototype"))
        }
    }

    private fun find_issue_links(issue_link_entities: EntityIterable)
    {
        for(entity in issue_link_entities)
        {
            issue_links.add(
                IssueLink(
                    entity.getProperty("sourceToTarget") as String,
                    entity.getProperty("targetToSource") as String,
                    entity.getProperty("sourceToTargetAssociationName") as String,
                    entity.getProperty("targetToSourceAssociationName") as String
                )
            )
        }
    }

    @JvmRecord
    private data class IssueLink(
        val s_to_t_name: String,
        val t_to_s_name: String,
        val s_to_t_association: String,
        val t_to_s_association: String
    )
}
