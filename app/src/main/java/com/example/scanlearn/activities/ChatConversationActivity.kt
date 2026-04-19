package com.example.scanlearn.activities

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.scanlearn.adapters.ChatMessageAdapter
import com.example.scanlearn.databinding.ActivityChatConversationBinding
import com.example.scanlearn.models.User
import com.example.scanlearn.services.RealtimeDbService
import com.example.scanlearn.services.StorageService
import com.example.scanlearn.utils.AppConstants
import com.google.firebase.database.ValueEventListener

class ChatConversationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityChatConversationBinding
    private lateinit var storage: StorageService
    private lateinit var dbService: RealtimeDbService
    private lateinit var messageAdapter: ChatMessageAdapter
    private var currentUser: User? = null
    private var chatPartner: User? = null
    private var messagesListener: ValueEventListener? = null
    private var conversationId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatConversationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        storage = StorageService(this)
        dbService = RealtimeDbService()
        currentUser = storage.getUser()

        val user = currentUser
        val partnerId = intent.getStringExtra(AppConstants.EXTRA_CHAT_PARTNER_ID).orEmpty()
        val partnerName = intent.getStringExtra(AppConstants.EXTRA_CHAT_PARTNER_NAME).orEmpty()
        val partnerRole = intent.getStringExtra(AppConstants.EXTRA_CHAT_PARTNER_ROLE).orEmpty()

        if (user == null || partnerId.isBlank()) {
            finish()
            return
        }

        chatPartner = User(id = partnerId, name = partnerName, role = partnerRole)
        conversationId = listOf(user.id, partnerId).sorted().joinToString("_")

        binding.tvChatTitle.text = partnerName.ifBlank { "Chat" }
        binding.tvChatSubtitle.text = partnerRole.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase() else it.toString()
        }.ifBlank { "User" }

        messageAdapter = ChatMessageAdapter(user.id)
        binding.rvMessages.layoutManager = LinearLayoutManager(this).apply {
            stackFromEnd = true
        }
        binding.rvMessages.adapter = messageAdapter

        binding.btnBack.setOnClickListener { finish() }
        binding.btnSend.setOnClickListener { sendMessage() }
    }

    override fun onStart() {
        super.onStart()
        markAsRead()
        observeMessages()
    }

    override fun onStop() {
        super.onStop()
        dbService.removeMessagesListener(conversationId, messagesListener)
        messagesListener = null
    }

    private fun observeMessages() {
        if (conversationId.isBlank()) return
        messagesListener = dbService.observeMessages(conversationId) { messages ->
            runOnUiThread {
                messageAdapter.submitList(messages)
                binding.tvEmptyMessages.visibility = if (messages.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
                if (messages.isNotEmpty()) {
                    binding.rvMessages.scrollToPosition(messages.lastIndex)
                }
            }
        }
    }

    private fun markAsRead() {
        val userId = currentUser?.id.orEmpty()
        if (conversationId.isBlank() || userId.isBlank()) return
        dbService.markConversationAsRead(conversationId, userId)
    }

    private fun sendMessage() {
        val sender = currentUser ?: return
        val receiver = chatPartner ?: return
        val text = binding.etMessage.text?.toString().orEmpty()

        if (!dbService.canUsersChat(sender, receiver)) {
            Toast.makeText(this, "This chat is not allowed.", Toast.LENGTH_SHORT).show()
            return
        }

        binding.btnSend.isEnabled = false
        dbService.sendChatMessage(sender, receiver, text) { success, error ->
            runOnUiThread {
                binding.btnSend.isEnabled = true
                if (success) {
                    binding.etMessage.text?.clear()
                } else {
                    Toast.makeText(
                        this,
                        error ?: "Could not send the message.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
}
