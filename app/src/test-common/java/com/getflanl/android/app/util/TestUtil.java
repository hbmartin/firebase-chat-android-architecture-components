/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package me.haroldmartin.chat.util;

import me.haroldmartin.chat.vo.ChatMessage;
import me.haroldmartin.chat.vo.Contributor;
import me.haroldmartin.chat.vo.User;

import java.util.ArrayList;
import java.util.List;

public class TestUtil {

    public static User createUser(String login) {
        return new User(login, null,
                login + " name", null, null, null);
    }

    public static List<ChatMessage> createRepos(int count, String owner, String name,
                                                String description) {
        List<ChatMessage> repos = new ArrayList<>();
        for(int i = 0; i < count; i ++) {
            repos.add(createRepo(owner + i, name + i, description + i));
        }
        return repos;
    }

    public static ChatMessage createRepo(String owner, String name, String description) {
        return createRepo(ChatMessage.UNKNOWN_ID, owner, name, description);
    }

    public static ChatMessage createRepo(int id, String owner, String name, String description) {
        return new ChatMessage(id, name, owner + "/" + name,
                description, new ChatMessage.Owner(owner, null), 3);
    }

    public static Contributor createContributor(ChatMessage repo, String login, int contributions) {
        Contributor contributor = new Contributor(login, contributions, null);
        contributor.setRepoName(repo.name);
        contributor.setRepoOwner(repo.owner.login);
        return contributor;
    }
}
