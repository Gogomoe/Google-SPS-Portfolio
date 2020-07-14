// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

async function loadComments() {
    const result = await (await fetch('/comment', {method: 'GET'})).json()

    const commentListElement = document.getElementById('comment-list')
    commentListElement.innerHTML = ''

    result.comments.forEach((comment) => {
        commentListElement.appendChild(createCommentElement(comment))
    })
}


function createCommentElement(comment) {
    const commentElement = document.createElement('div')
    commentElement.className = 'comment'
    commentElement.innerText = comment.content

    return commentElement;
}

function deleteComment(comment) {
    fetch(`/comment/${comment.id}`, {method: 'DELETE'})
}

async function submitComment() {
    const content = document.getElementById('comment-input').value

    await fetch('/comment', {
        method: 'POST',
        body: JSON.stringify({content: content})
    })
    await loadComments()
}

document.addEventListener('DOMContentLoaded', loadComments);