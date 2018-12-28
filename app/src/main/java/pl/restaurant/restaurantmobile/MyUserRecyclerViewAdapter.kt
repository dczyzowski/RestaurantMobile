package pl.restaurant.restaurantmobile

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

import pl.restaurant.restaurantmobile.fragments.UserListFragment.OnListFragmentInteractionListener

import kotlinx.android.synthetic.main.fragment_user.view.*
import pl.restaurant.restaurantmobile.database.User
import pl.restaurant.restaurantmobile.database.UsersDatabase

class MyUserRecyclerViewAdapter(
        private val mValues: List<User>,
        private val mListener: OnListFragmentInteractionListener?)
    : RecyclerView.Adapter<MyUserRecyclerViewAdapter.ViewHolder>() {

    private val mOnClickListener: View.OnClickListener
    private val mOnLongClickListener: View.OnLongClickListener
    lateinit var context : Context

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as User
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
        mOnLongClickListener = View.OnLongClickListener { v ->
            val item = v.tag as User
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            AlertDialog.Builder(context)
                    .setCancelable(true)
                    .setTitle(R.string.delete_title)
                    .setMessage(context.getString(R.string.delete_prompt, item.firstName))
                    .setNegativeButton(R.string.delete
                    ) { _, _ ->
                        UsersDatabase.getInstance(context)!!.userDao().delete(user = item) }.create().show()
            true
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        context = parent.context
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.fragment_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mValues[position]
        if(item.avatar != null)
            holder.mIdView.setImageDrawable(ContextCompat.getDrawable(context, item.avatar!!))
        holder.mContentView.text = item.firstName

        with(holder.mView) {
            tag = item
            setOnClickListener(mOnClickListener)
            setOnLongClickListener(mOnLongClickListener)
        }
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        val mIdView: ImageView = mView.image
        val mContentView: TextView = mView.content

        override fun toString(): String {
            return super.toString() + " '" + mContentView.text + "'"
        }
    }
}
