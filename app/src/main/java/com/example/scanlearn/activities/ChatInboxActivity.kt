package com.example.scanlearn.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.ChatContactAdapter
import com.example.scanlearn.adapters.ChatConversationAdapter
import com.example.scanlearn.databinding.ActivityChatInboxBinding
import com.example.scanlearn.models.User
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.google.firebase.database.ValueEventListener

class ChatInboxActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatInboxBinding
    private lateinit var storage: StorageService
    private lateinit var dbService: RealtimeDbService
    private lateinit var conversationAdapter: ChatConversationAdapter
    private lateinit var contactAdapter: ChatContactAdapter
    private var currentUser: User? = null
    private var conversationsListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatInboxBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        dbService = RealtimeDbService()
        currentUser = storage.getUser()

        val user = currentUser
        if (user == null) {
            finish()
            return
        }

        binding.tvInboxTitle.text = if (user.role.equals("teacher", ignoreCase = true)) {
            "Teacher Messages"
        } else {
            "Message a Teacher"
        }
        binding.tvInboxSubtitle.text = if (user.role.equals("teacher", ignoreCase = true)) {
            "Chat with students or other teachers from the school database."
        } else {
            "Reach out to any teacher in the database when you need help."
        }

        conversationAdapter = ChatConversationAdapter(user.id) { conversation ->
            val partnerId = conversation.participantIds.firstOrNull { it != user.id }.orEmpty()
            val partnerName = conversation.participantNames[partnerId].orEmpty()
            val partnerRole = conversation.participantRoles[partnerId].orEmpty()
            openConversation(
                User(
                    id = partnerId,
                    name = partnerName,
                    role = partnerRole
                )
            )
        }
        contactAdapter = ChatContactAdapter { chatUser ->
            openConversation(chatUser)
        }

        binding.rvConversations.layoutManager = LinearLayoutManager(this)
        binding.rvConversations.adapter = conversationAdapter
        binding.rvContacts.layoutManager = LinearLayoutManager(this)
        binding.rvContacts.adapter = contactAdapter

        binding.btnBack.setOnClickListener { finish() }
    }

    override fun onResume() {
        super.onResume()
        loadContacts()
        observeInbox()
    }

    override fun onPause() {
        super.onPause()
        dbService.removeConversationsListener(conversationsListener)
        conversationsListener = null
    }

    private fun loadContacts() {
        val user = currentUser ?: return
        setLoading(true)

        dbService.getChatContacts(user) { contacts ->
            runOnUiThread {
                setLoading(false)
                contactAdapter.submitList(contacts)
                binding.tvContactsLabel.text = if (user.role.equals("teacher", ignoreCase = true)) {
                    "Start a new chat"
                } else {
                    "Teachers available"
                }
                binding.emptyContactsState.visibility =
                    if (contacts.isEmpty()) View.VISIBLE else View.GONE
                binding.rvContacts.visibility = if (contacts.isEmpty()) View.GONE else View.VISIBLE
            }
        }
    }

    private fun observeInbox() {
        val user = currentUser ?: return
        dbService.removeConversationsListener(conversationsListener)
        conversationsListener = dbService.observeConversationsForUser(user.id) { conversations ->
            runOnUiThread {
                conversationAdapter.submitList(conversations)
                binding.emptyConversationState.visibility =
                    if (conversations.isEmpty()) View.VISIBLE else View.GONE
                binding.rvConversations.visibility =
                    if (conversations.isEmpty()) View.GONE else View.VISIBLE
                val unreadThreads = dbService.getUnreadConversationCount(conversations, user.id)
                binding.tvInboxSubtitle.text = if (unreadThreads > 0) {
                    "$unreadThreads conversation(s) need your attention."
                } else if (user.role.equals("teacher", ignoreCase = true)) {
                    "Chat with students or other teachers from the school database."
                } else {
                    "Reach out to any teacher in the database when you need help."
                }
            }
        }
    }

    private fun setLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.contentGroup.visibility = if (isLoading) View.GONE else View.VISIBLE
    }

    private fun openConversation(chatUser: User) {
        val current = currentUser ?: return
        if (!dbService.canUsersChat(current, chatUser)) {
            Toast.makeText(this, "This chat is not allowed.", Toast.LENGTH_SHORT).show()
            return
        }

        val intent = Intent(this, ChatConversationActivity::class.java).apply {
            putExtra(AppConstants.EXTRA_CHAT_PARTNER_ID, chatUser.id)
            putExtra(AppConstants.EXTRA_CHAT_PARTNER_NAME, chatUser.name)
            putExtra(AppConstants.EXTRA_CHAT_PARTNER_ROLE, chatUser.role)
        }
        startActivity(intent)
    }
}
