/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// Get ?user=XYZ parameter value
const urlParams = new URLSearchParams(window.location.search);
const parameterUsername = urlParams.get('user');

// URL must include ?user=XYZ parameter. If not, redirect to homepage.
if (!parameterUsername) {
  window.location.replace('/');
}

/** Sets the page title based on the URL parameter username. */
function setPageTitle() {
  document.getElementById('page-title').innerText = parameterUsername;
  document.title = parameterUsername + ' - User Page';
}

/**
 * Shows the message form if the user is logged in and viewing their own page.
 */
function showMessageFormIfViewingSelf() {
  fetch('/login-status')
      .then((response) => {
        return response.json();
      })
      .then((loginStatus) => {
        if (loginStatus.isLoggedIn && loginStatus.username == parameterUsername) {
          const messageForm = document.getElementById('message-form');
          messageForm.classList.remove('hidden');
        }
      });
  document.getElementById('about-me-form').classList.remove('hidden');
}

/** Fetches messages and add them to the page. */
function fetchMessages() {
  const url = '/messages?user=' + parameterUsername;
  fetch(url)
      .then((response) => {
        return response.json();
      })
      .then((messages) => {
        const messagesContainer = document.getElementById('message-container');
        if (messages.length == 0) {
          messagesContainer.innerHTML = '<p>This user has no posts yet.</p>';
        } else {
          messagesContainer.innerHTML = '';
        }
        messages.forEach((message) => {
          const messageDiv = buildMessageDiv(message);
          messagesContainer.appendChild(messageDiv);
        });
      });
}

/**
 * Builds an element that displays the message.
 * @param {Message} message
 * @return {Element}
 */
function buildMessageDiv(message) {
  const headerDiv = document.createElement('div');
  headerDiv.classList.add('message-header');
  headerDiv.appendChild(document.createTextNode(
      message.user + ' - ' + new Date(message.timestamp)));
  
  // Create label
  let label = document.createElement("span");
  label.className = "label";
  if (message.isFulfilled) {
    label.innerHTML = "Fulfilled";
    label.style.backgroundColor = "#008000";
  }
  else {
    label.innerHTML = "Still Available";
    label.style.backgroundColor = "#4CAF50";
  }
  headerDiv.appendChild(label);

  // Give user a fullfilment button
  const setAsFulflledButton = document.createElement("button");
  setAsFulflledButton.innerHTML = "Set as fulfilled";
  setAsFulflledButton.style.alignSelf = "right";
  setAsFulflledButton.addEventListener("click", function() {
    const messageIdentifier = message.id + " " + message.user;
    
    fetch('/label-editor', {
      method: 'POST',
      body: JSON.stringify(messageIdentifier),
      headers: {
        'Content-Type': 'application/json'
      }
    })
    .then((response) => {
      window.location.replace(response.url);
    })
  })

  // Give user a delete button to delete a message
  const deleteButton = document.createElement("button");
  deleteButton.innerHTML = "Delete post";
  deleteButton.style.alignSelf = "right";
  deleteButton.addEventListener("click", function() {
    const messageIdentifier = message.id + " " + message.user;

    fetch('/entity-remover', {
      method: 'POST',
      body: JSON.stringify(messageIdentifier),
      headers: {
        'Content-Type': 'application/json'
      }
    })
    .then((response) => {
      window.location.replace(response.url);
    })
  })

  // Give user an edit button to edit an existing message
  const editButton = document.createElement("button");
  editButton.innerHTML = "Edit post";
  editButton.style.alignSelf = "right";
  editButton.addEventListener("click", function() {

    // Create a form and its div
    const form = document.createElement("form");
    const formDiv = document.createElement('div');
    
    // Create a submit button
    const submitButton = document.createElement("input");
    submitButton.setAttribute("type", "submit");
    submitButton.setAttribute("value", "Submit");

    // Create text area for the new edited text
    const textBox = document.createElement("textarea");
    textBox.setAttribute("name", "text");
    textBox.setAttribute("id", "edited-message-input");

    // Take note of the messageId in a textarea element. This textbox is hidden
    // from the user and is only used by the servlet
    const messageId = document.createElement("textarea");
    messageId.value = message.id;
    messageId.setAttribute("name", "messageId");
    messageId.setAttribute("style", "display:none");

    // Add a new div to the header for the form text box
    headerDiv.appendChild(formDiv);
    formDiv.appendChild(form);

    // Set the form attributes
    form.setAttribute("id", "edited-message-form");
    form.setAttribute("action", "/entity-editor");
    form.setAttribute("method", "POST");
    form.appendChild(textBox);
    form.appendChild(messageId);
    form.appendChild(submitButton);
  })
  
  // Add all buttons to the header of the message
  headerDiv.appendChild(setAsFulflledButton);
  headerDiv.appendChild(deleteButton);
  headerDiv.appendChild(editButton);
  
  const bodyDiv = document.createElement('div');
  bodyDiv.classList.add('message-body');
  bodyDiv.innerHTML = message.text;

  const messageDiv = document.createElement('div');
  messageDiv.classList.add('message-div');
  messageDiv.appendChild(headerDiv);
  messageDiv.appendChild(bodyDiv);
  
  return messageDiv;
}

/** Fetches data and populates the aboutme container */
function fetchAboutMe(){
  const url = '/about?user=' + parameterUsername;
  fetch(url).then((response) => {
    return response.text();
  }).then((aboutMe) => {
    const aboutMeContainer = document.getElementById('about-me-container');
    if(aboutMe == ''){
      aboutMe = 'This user has not entered any information yet.';
    }
    
    aboutMeContainer.innerHTML = aboutMe;
  });
}

/** Fetches data and populates the UI of the page. */
function buildUI() {
  setPageTitle();
  showMessageFormIfViewingSelf();
  fetchMessages();
  fetchAboutMe();
}
