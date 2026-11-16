
class LRUCache {
    private:

    int capacity;

    unordered_map<int, Node*> mp;

    Node* head;
    Node* tail;

    // 1. Delete a node from DLL
    void deleteNode(Node* node) {

        Node* prevNode = node->prev;
        Node* nextNode = node->next;

        prevNode->next = nextNode;
        nextNode->prev = prevNode;
    }

    // 2. Insert node after head
    void addNode(Node* node) {

        Node* nextNode = head->next;

        head->next = node;
        node->prev = head;

        node->next = nextNode;
        nextNode->prev = node;
    }

    // 3. Move node to MRU position
    void moveToFront(Node* node) {

        deleteNode(node);
        addNode(node);
    }

    // 4. Remove least recently used node
    void removeLRU() {

        Node* lru = tail->prev;

        mp.erase(lru->key);

        deleteNode(lru);

        delete lru;
    }

    // 5. Insert new key-value pair
    void insert(int key, int value) {

        Node* node = new Node(key, value);

        addNode(node);

        mp[key] = node;
    }

    public:

    // 6. Constructor
    LRUCache(int capacity) {

        this->capacity = capacity;

        head = new Node(-1, -1);
        tail = new Node(-1, -1);

        head->next = tail;
        tail->prev = head;
    }

    // 7. Get operation
    int get(int key) {

        if (mp.find(key) == mp.end()) {
            return -1;
        }

        Node* node = mp[key];

        int ans = node->value;

        moveToFront(node);

        return ans;
    }

    // 8. Put operation
    void put(int key, int value) {

        // Key already exists
        if (mp.find(key) != mp.end()) {

            Node* node = mp[key];

            node->value = value;

            moveToFront(node);

            return;
        }

        // Cache is full
        if (mp.size() == capacity) {
            removeLRU();
        }

        // Insert new node
        insert(key, value);
    }
};
