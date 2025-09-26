#  Notes Manager (Java Console App)

A simple **text-based Notes Manager** written in Java.  
This program allows you to create, view, edit, search, and delete notes stored as text files on your computer.

---

##  Features
- Create multiple notes at once
- Each note is automatically assigned a unique **ID**
- Save notes as files (`id_title.txt`)
- View a note by its ID
- Edit a note by ID
- Delete a single note by ID
- Delete **all notes** with confirmation
- Search notes by keyword (in title or content)
- Stores all notes in a local `notes/` folder

---

##  Project Structure
```

NotesManager/
│
├── src/
│   └── NotesManager.java   # Main program
│
├── notes/                  # Folder where notes are stored (auto-created)
│
└── README.md               # Project documentation

````

---

##  Menu Options

```
===== Notes Manager =====
1. Create new notes
2. View a note (by ID)
3. Edit a note (by ID)
4. Delete a note (by ID)
5. Search notes
6. Delete ALL notes
0. Exit
```

---

##  Example Usage

* Create 2 notes:

  * Title: `Shopping List`
  * Content:

    ```
    Milk
    Eggs
    Bread
    .
    ```
  * Title: `To-Do`
  * Content:

    ```
    Finish assignment
    Call John
    .
    ```

  Saved as:

  ```
  1_Shopping_List.txt
  2_To_Do.txt
  ```

* View note `1` → shows Shopping List.

* Edit note `2` → update To-Do list.

* Search keyword `"Milk"` → finds note `1`.

* Delete note `2` → removes To-Do.

* Delete ALL → wipes the `notes/` folder.

---

##  Future Improvements

* Add password protection
* Export all notes into a single file
* Tagging & categories
* GUI version with Swing or JavaFX

---
