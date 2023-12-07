package net.accelf.yuito

import android.text.Spanned
import android.view.View
import androidx.annotation.Px
import com.keylesspalace.tusky.R
import com.keylesspalace.tusky.databinding.ViewQuoteInlineBinding
import com.keylesspalace.tusky.entity.Emoji
import com.keylesspalace.tusky.entity.HashTag
import com.keylesspalace.tusky.entity.Status.Mention
import com.keylesspalace.tusky.interfaces.LinkListener
import com.keylesspalace.tusky.util.StatusDisplayOptions
import com.keylesspalace.tusky.util.emojify
import com.keylesspalace.tusky.util.loadAvatar
import com.keylesspalace.tusky.util.setClickableText
import com.keylesspalace.tusky.viewdata.StatusViewData

class QuoteInlineHelper(
    private val binding: ViewQuoteInlineBinding,
    private val listener: LinkListener,
    @Px private val avatarRadius24dp: Int,
    private val statusDisplayOptions: StatusDisplayOptions,
) {

    private fun setDisplayName(name: String, customEmojis: List<Emoji>?) {
        val viewDisplayName = binding.statusQuoteInlineDisplayName
        val emojifiedName = name.emojify(customEmojis, viewDisplayName, statusDisplayOptions.animateEmojis)
        viewDisplayName.text = emojifiedName
    }

    private fun setUsername(name: String) {
        val viewUserName = binding.statusQuoteInlineUsername
        val context = viewUserName.context
        val format = context.getString(R.string.post_username_format)
        val usernameText = String.format(format, name)
        viewUserName.text = usernameText
    }

    private fun setContent(
        content: Spanned,
        mentions: List<Mention>,
        tags: List<HashTag>?,
        emojis: List<Emoji>,
    ) {
        val viewContent = binding.statusQuoteInlineContent
        val singleLineText = SpannedTextHelper.replaceSpanned(content)
        val emojifiedText = singleLineText.emojify(emojis, viewContent, statusDisplayOptions.animateEmojis)
        setClickableText(viewContent, emojifiedText, mentions, tags, listener)
    }

    private fun setAvatar(url: String, @Px avatarRadius24dp: Int, statusDisplayOptions: StatusDisplayOptions) {
        loadAvatar(url, binding.statusQuoteInlineAvatar, avatarRadius24dp, statusDisplayOptions.animateAvatars)
    }

    private fun setSpoilerText(spoilerText: String, emojis: List<Emoji>) {
        val viewDescription = binding.statusQuoteInlineContentWarningDescription
        val viewButton = binding.statusQuoteInlineContentWarningButton
        val emojiSpoiler = spoilerText.emojify(emojis, viewDescription, statusDisplayOptions.animateEmojis)
        viewDescription.text = emojiSpoiler
        viewDescription.visibility = View.VISIBLE
        viewButton.visibility = View.VISIBLE
        viewButton.setOnClickListener {
            setContentVisibility(binding.statusQuoteInlineContent.visibility != View.VISIBLE)
        }
        setContentVisibility(false)
    }

    private fun setContentVisibility(show: Boolean) {
        binding.statusQuoteInlineContent.visibility = when (show) {
            true -> View.VISIBLE
            false -> View.GONE
        }
        binding.statusQuoteInlineContentWarningButton.setText(
            when (show) {
                true -> R.string.post_content_warning_show_less
                false -> R.string.post_content_warning_show_more
            }
        )
    }

    private fun hideSpoilerText() {
        binding.statusQuoteInlineContentWarningDescription.visibility = View.GONE
        binding.statusQuoteInlineContentWarningButton.visibility = View.GONE
        binding.statusQuoteInlineContent.visibility = View.VISIBLE
    }

    private fun setOnClickListener(accountId: String, statusUrl: String?) {
        binding.statusQuoteInlineAvatar.setOnClickListener { listener.onViewAccount(accountId) }
        binding.statusQuoteInlineDisplayName.setOnClickListener { listener.onViewAccount(accountId) }
        binding.statusQuoteInlineUsername.setOnClickListener { listener.onViewAccount(accountId) }
        binding.statusQuoteInlineContent.setOnClickListener { listener.onViewUrl(statusUrl!!, statusUrl) }
        binding.statusQuoteInlineMedia.setOnClickListener { listener.onViewUrl(statusUrl!!, statusUrl) }
        binding.root.setOnClickListener { listener.onViewUrl(statusUrl!!, statusUrl) }
    }

    fun setupQuoteContainer(quote: StatusViewData.Concrete) {
        val actionable = quote.actionable
        val account = actionable.account
        setDisplayName(account.name, account.emojis)
        setUsername(account.username)
        setContent(
            quote.content,
            actionable.mentions,
            actionable.tags,
            actionable.emojis,
        )
        setAvatar(account.avatar, avatarRadius24dp, statusDisplayOptions)
        setOnClickListener(account.id, actionable.url)
        if (quote.status.spoilerText.isEmpty()) {
            hideSpoilerText()
        } else {
            setSpoilerText(quote.status.spoilerText, actionable.emojis)
        }
        val viewMedia = binding.statusQuoteInlineMedia
        if (actionable.attachments.size == 0) {
            viewMedia.visibility = View.GONE
        } else {
            viewMedia.visibility = View.VISIBLE
            viewMedia.text = viewMedia.context.getString(R.string.status_quote_media, actionable.attachments.size)
        }
    }
}
