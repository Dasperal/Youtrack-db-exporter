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

import jetbrains.exodus.entitystore.Entity
import jetbrains.exodus.entitystore.EntityIterable
import jetbrains.exodus.entitystore.PersistentEntityStores
import jetbrains.exodus.entitystore.StoreTransaction
import jetbrains.exodus.env.Environments.newInstance
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.StringJoiner

object Main
{
    private val issue_links: MutableList<IssueLink> = ArrayList()

    // Arg0 - path to youtrack db
    // Arg1 - path to export directory
    @JvmStatic
    fun main(args: Array<String>)
    {
        val env = newInstance(args[0])
        val export_root = Paths.get(args[1])
        val store = PersistentEntityStores.newInstance(env, "teamsysstore")
        store.executeInTransaction {
            find_issue_links(it.getAll("IssueLinkPrototype"))
            export_issue_links(issues_with_links(it), export_root)
            export_issue_comments(it.findWithLinks("Issue", "comments"), export_root)
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

    private fun issues_with_links(txn: StoreTransaction): EntityIterable
    {
        var issue_entities =
            txn.findWithLinks("Issue", issue_links[0].s_to_t_association)
                .union(txn.findWithLinks("Issue", issue_links[0].t_to_s_association))
        for(link in issue_links.subList(1, issue_links.size))
        {
            issue_entities =
                issue_entities.union(txn.findWithLinks("Issue", link.s_to_t_association))
                    .union(txn.findWithLinks("Issue", link.t_to_s_association))
        }
        return issue_entities
    }

    private fun export_issue_links(issue_entities: EntityIterable, export_root: Path)
    {
        val csv = StringJoiner("\n")
        val header = StringJoiner(";").add("id")
        for(link in issue_links)
        {
            header.add(link.s_to_t_name)
            header.add(link.t_to_s_name)
        }
        csv.add(header.toString())

        for(entity in issue_entities)
        {
            val line = StringJoiner(";").add(issue_id(entity))
            for(link in issue_links)
            {
                line.add(linked_issue_ids(entity, link.s_to_t_association))
                line.add(linked_issue_ids(entity, link.t_to_s_association))
            }
            csv.add(line.toString())
        }
        val links_file_path = export_root.resolve("issue_links.csv")
        println("Writing issue links to \"${links_file_path.toAbsolutePath()}\"")
        Files.writeString(links_file_path, csv.toString())
    }

    private fun linked_issue_ids(entity: Entity, link_name: String): String
    {
        val links = StringJoiner(",")
        for(issue in entity.getLinks(link_name))
        {
            links.add(issue_id(issue))
        }
        return links.toString()
    }

    private fun export_issue_comments(issue_entities: EntityIterable, export_root: Path)
    {
        val csv = StringJoiner("\n")
        csv.add("issue;created_at;author;text")

        for(entity in issue_entities)
        {
            for(comment in entity.getLinks("comments"))
            {
                val line = StringJoiner(";").add(issue_id(entity))
                line.add((comment.getProperty("created") as Long).toString())
                line.add((comment.getLink("author")?.getProperty("login") as String))
                line.add("\"${comment.getBlobString("text")?.replace("\"", "\"\"")}\"")
                csv.add(line.toString())
            }
        }

        val links_file_path = export_root.resolve("issue_comments.csv")
        println("Writing issue comments to \"${links_file_path.toAbsolutePath()}\"")
        Files.writeString(links_file_path, csv.toString())
    }

    private fun issue_id(entity: Entity): String
    {
        val number = entity.getProperty("numberInProject") as Long
        val project_prefix = entity.getLink("project")?.getProperty("shortName") as String
        return "$project_prefix-$number"
    }

    @JvmRecord
    private data class IssueLink(
        val s_to_t_name: String,
        val t_to_s_name: String,
        val s_to_t_association: String,
        val t_to_s_association: String
    )
}
